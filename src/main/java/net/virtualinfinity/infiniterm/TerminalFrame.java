package net.virtualinfinity.infiniterm;

import net.virtualinfinity.emulation.Decoder;
import net.virtualinfinity.emulation.Encoder;
import net.virtualinfinity.emulation.KeyListenerInputDevice;
import net.virtualinfinity.emulation.LocalTerminalTypeHandler;
import net.virtualinfinity.emulation.telnet.TelnetDecoder;
import net.virtualinfinity.emulation.ui.OutputDeviceImpl;
import net.virtualinfinity.emulation.ui.PresentationComponent;
import net.virtualinfinity.nio.EventLoop;
import net.virtualinfinity.swing.StickyBottomScrollPane;
import net.virtualinfinity.telnet.Option;
import net.virtualinfinity.telnet.TelnetSession;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Vector;
import java.util.function.Consumer;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public class TerminalFrame {
    private final PresentationComponent view;
    private final JFrame frame;
    private final JComboBox<String> hostInput;
    private final JComboBox<Charset> encodingInput;
    private final EventLoop eventLoop;
    private State state = State.DISCONNECTED;

    public TerminalFrame(EventLoop eventLoop) {
        this.eventLoop = eventLoop;
        this.frame = new JFrame();
        view = new PresentationComponent();
        frame.add(new StickyBottomScrollPane(view));

        // TODO: Font picker and preferences.
        frame.setFont(new Font("PT Mono", Font.PLAIN, 16));
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.addWindowStateListener(e -> {
            if (e.getNewState() == WindowEvent.WINDOW_CLOSING) {
                disconnect();
            }
        });
        final JToolBar toolBar = new JToolBar();
        toolBar.add(new JLabel("Host:"));
        hostInput = new JComboBox<>();
        hostInput.setEditable(true);
        hostInput.addActionListener(new ConnectAction());
        toolBar.add(hostInput);
        toolBar.add(new JButton(new ConnectAction()));
        toolBar.addSeparator();
        toolBar.add(new JLabel("Encoding:"));
        //noinspection UseOfObsoleteCollectionType
        encodingInput = new JComboBox<>(new Vector<>(Charset.availableCharsets().values()));
        encodingInput.setEditable(false);
        encodingInput.setSelectedItem(Charset.defaultCharset());
        toolBar.add(encodingInput);
        frame.add(toolBar, BorderLayout.PAGE_START);
    }

    public void show() {
        frame.pack();
        frame.setVisible(true);
    }

    private void connect() {
        if (state != State.DISCONNECTED) {
            return;
        }
        final Object selectedItem = hostInput.getModel().getSelectedItem();
        if (selectedItem == null) {
            JOptionPane.showMessageDialog(frame,
                "Please select a host to connect to.",
                "No host selected", JOptionPane.ERROR_MESSAGE);
            return;
        }
        state = State.CONNECTING;
        final Charset charset = encodingInput.getItemAt(encodingInput.getSelectedIndex());
        try (final SocketChannel channel = SocketChannel.open()) {
            channel.configureBlocking(false);
            if (channel.connect(parseAddress(selectedItem.toString()))) {
                finishConnect(charset, channel);
            } else {
                eventLoop.registerHandler(channel, SelectionKey.OP_CONNECT, () -> finishConnect(charset, channel));
            }

        } catch (Exception e) {
            disconnect();
            displayConnectError(e);
        }
    }

    private void finishConnect(Charset charset, SocketChannel channel) throws ClosedChannelException {
        try {
            channel.finishConnect();
        } catch (IOException e) {
            disconnect();
            displayConnectError(e);
            return;
        }
        state = State.CONNECTED;
        final OutputDeviceImpl device = new OutputDeviceImpl(view.getModel());
        final Decoder decoder = new Decoder(device).charset(charset);
        final TelnetSession telnetSession = createTelnetSession(channel, decoder);
        final Encoder encoder = new Encoder(new TelnetSessionDispatcher(telnetSession, eventLoop)).charset(charset);
        final KeyListenerInputDevice inputDevice = new KeyListenerInputDevice(encoder);
        view.getInputMap().put(KeyStroke.getKeyStroke("meta V"), "paste");
        view.getActionMap().put("paste", new PasteAction(encoder));
        view.addKeyListener(inputDevice);

        eventLoop.registerHandler(channel, telnetSession);
    }

    private void displayConnectError(Exception e) {
        JOptionPane.showMessageDialog(frame,
            "Unable to connect to host:" + e.toString(),
            "Connection error.", JOptionPane.ERROR_MESSAGE);
    }

    private TelnetSession createTelnetSession(SocketChannel channel, Decoder decoder) {
        final TelnetSession session = new TelnetSession(channel, new TelnetDecoder(decoder));
        session.option(Option.ECHO).allowRemote();
        session.option(Option.BINARY_TRANSMISSION).allowRemote().allowLocal();
        session.option(Option.SUPPRESS_GO_AHEAD).requestRemoteEnable().allowLocal();
        session.installOptionReceiver(new LocalTerminalTypeHandler()).allowLocal();
        return session;
    }

    private SocketAddress parseAddress(String host) {
        final int colon = host.indexOf(':');
        if (colon < 0) {
            return new InetSocketAddress(host, 23);
        }

        return new InetSocketAddress(host.substring(0, colon), Integer.parseInt(host.substring(colon+1)));
    }

    public void disconnect() {
    }


    private class ConnectAction extends AbstractAction {
        public ConnectAction() {
            super("Connect");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            connect();
        }
    }

    private enum State {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
    }

    private static class TelnetSessionDispatcher implements Consumer<ByteBuffer> {
        private final TelnetSession telnetSession;
        private final EventLoop eventLoop;

        public TelnetSessionDispatcher(TelnetSession telnetSession, EventLoop eventLoop) {
            this.telnetSession = telnetSession;
            this.eventLoop = eventLoop;
        }

        @Override
        public void accept(ByteBuffer buffer) {
            ByteBuffer copy = ByteBuffer.allocate(buffer.remaining());
            copy.put(buffer);
            copy.flip();
            eventLoop.invokeLater(() -> {
                telnetSession.writeData(copy);
            });
        }
    }

    private static class PasteAction extends AbstractAction {
        private final Encoder encoder;

        public PasteAction(Encoder encoder) {
            this.encoder = encoder;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            final Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();

            final Transferable contents = cb.getContents(null);
            if (contents == null) {
                return;
            }
            try {
                if (contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    DataFlavor.getTextPlainUnicodeFlavor().getReaderForText(contents);
                    final String transferData = (String)contents.getTransferData(DataFlavor.stringFlavor);
                    encoder.character(CharBuffer.wrap(transferData));
                    encoder.flush(false);
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

}
