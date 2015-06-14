package net.virtualinfinity.infiniterm;

import net.virtualinfinity.emulation.*;
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
public class Terminal {
    public static final String IBM_437 = "IBM437";
    public static final String TITLE_PREFIX = "VirtualInfiniterm";
    public static final String DISCONNECTED_TITLE = TITLE_PREFIX + " - (Disconnected)";
    private final PresentationComponent view;
    private final JFrame frame;
    private final JComboBox<String> hostInput;
    private final JComboBox<Charset> encodingInput;
    private final EventLoop eventLoop;
    private final KeyListenerInputDevice inputDevice = new KeyListenerInputDevice();
    private final Action connectAction = new AbstractAction("Connect") {
        @Override
        public void actionPerformed(ActionEvent e) {
            connect();
            view.requestFocusInWindow();
        }
    };
    private final Action disconnectAction = new AbstractAction("Disconnect") {
        {
            setEnabled(false);
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            disconnect();
            hostInput.requestFocusInWindow();
        }
    };
    private State state = State.DISCONNECTED;
    private final int id;
    private static int nextId = 0;
    private Decoder decoder;
    private Encoder encoder;
    private TelnetSession telnetSession;

    {
        synchronized (Terminal.class) {
            id = nextId++;
        }
    }

    public Terminal() throws IOException {
        this.eventLoop = new EventLoop(this::handleException);
        this.frame = new JFrame(DISCONNECTED_TITLE);
        view = new PresentationComponent();
        view.setFont(new Font("PT Mono", Font.PLAIN, 16));
        frame.add(new StickyBottomScrollPane(view));
        // TODO: Font picker and preferences.
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
        toolBar.add(hostInput);
        toolBar.add(new JButton(connectAction));
        toolBar.add(new JButton(disconnectAction));
        toolBar.addSeparator();
        toolBar.add(new JLabel("Encoding:"));
        //noinspection UseOfObsoleteCollectionType
        final Vector<Charset> charsets = new Vector<>();
        charsets.add(Charset.defaultCharset());
        if (Charset.isSupported(IBM_437)) {
            charsets.add(Charset.forName(IBM_437));
        }
        charsets.addAll(Charset.availableCharsets().values());
        encodingInput = new JComboBox<>(charsets);
        encodingInput.setEditable(false);
        encodingInput.setSelectedItem(Charset.defaultCharset());
        encodingInput.setAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onIOThread(() -> setCharSet(selectedCharset()));
            }
        });
        toolBar.add(encodingInput);
        frame.add(toolBar, BorderLayout.PAGE_START);
        view.getInputMap().put(KeyStroke.getKeyStroke("meta V"), "paste");
        view.getActionMap().put("paste", new PasteAction(inputDevice));
        view.addKeyListener(inputDevice);
    }

    private void handleException(SelectionKey selectionKey, IOException e) {
        e.printStackTrace();
    }

    public void show() {
        frame.pack();
        frame.setVisible(true);
        new Thread(() -> {
            try {
                eventLoop.run();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, "EventLoop-" + id).start();
    }

    private void connect() {
        onGuiThread(() -> {
            disconnectAction.setEnabled(true);
            final Object selectedItem = hostInput.getModel().getSelectedItem();
            if (selectedItem == null) {
                JOptionPane.showMessageDialog(frame,
                    "Please select a host to connect to.",
                    "No host selected", JOptionPane.ERROR_MESSAGE);
                return;
            }
            final InetSocketAddress address = parseAddress(selectedItem.toString());
            hostInput.setEnabled(false);
            connectAction.setEnabled(false);
            frame.setTitle(TITLE_PREFIX + " - " + addressName(address) + " (Connecting...)");
            onIOThread(() -> {
                state = State.CONNECTING;
                SocketChannel channel = null;
                try {
                    channel = SocketChannel.open(address);
                    channel.configureBlocking(false);
                    finishConnect(selectedCharset(), channel);
                    frame.setTitle(TITLE_PREFIX + " - " + addressName(address) + " (Connected)");
                } catch (Exception e) {
                    displayConnectError(e);
                    disconnect();
                    if (channel != null) {
                        try {
                            channel.close();
                        } catch (IOException ignore) {
                        }
                    }
                }
            });
        });
    }

    private String addressName(InetSocketAddress address) {
        if (address.getPort() != 23) {
            return address.getHostString() + ":" + address.getPort();
        }
        return address.getHostString();
    }

    private Charset selectedCharset() {
        return encodingInput.getItemAt(encodingInput.getSelectedIndex());
    }

    private void onIOThread(Runnable action) {
        eventLoop.invokeLater(action);
    }

    private void finishConnect(Charset charset, SocketChannel channel) throws ClosedChannelException {
        try {
            channel.finishConnect();
        } catch (IOException e) {
            displayConnectError(e);
            disconnect();
            return;
        }
        state = State.CONNECTED;
        final OutputDeviceImpl device = new OutputDeviceImpl(view.getModel());
        device.inputDevice(inputDevice);
        decoder = new Decoder(device);
        telnetSession = createTelnetSession(channel, decoder);
        encoder = new Encoder(new TelnetSessionDispatcher(telnetSession, eventLoop));
        setCharSet(charset);
        onGuiThread(() -> inputDevice.setEncoder(encoder));
    }

    private void setCharSet(Charset charset) {
        onIOThread(() -> {
            if (encoder != null) {
                encoder.charset(charset);
            }
            if (decoder != null) {
                decoder.charset(charset);
            }
        });
    }

    private void onGuiThread(Runnable action) {
        EventQueue.invokeLater(action);
    }

    private void displayConnectError(Exception e) {
        onGuiThread(() ->
            JOptionPane.showMessageDialog(frame,
                "Unable to connect to host:" + e.toString(),
                "Connection error.", JOptionPane.ERROR_MESSAGE));
    }

    private TelnetSession createTelnetSession(SocketChannel channel, Decoder decoder) throws ClosedChannelException {
        final TelnetSession session = new TelnetSession(channel, new TelnetDecoder(decoder, this::disconnect));
        eventLoop.registerHandler(channel, session);
        session.option(Option.ECHO).allowRemote();
        session.option(Option.BINARY_TRANSMISSION).allowRemote().allowLocal();
        session.option(Option.SUPPRESS_GO_AHEAD).requestRemoteEnable().allowLocal();
        session.installOptionReceiver(new LocalTerminalTypeHandler()).allowLocal();
        return session;
    }

    private InetSocketAddress parseAddress(String host) {
        final int colon = host.indexOf(':');
        if (colon < 0) {
            return new InetSocketAddress(host, 23);
        }

        return new InetSocketAddress(host.substring(0, colon), Integer.parseInt(host.substring(colon+1)));
    }

    public void disconnect() {
        onGuiThread(() -> {
            disconnectAction.setEnabled(false);
            hostInput.setEnabled(true);
            connectAction.setEnabled(true);
            frame.setTitle(DISCONNECTED_TITLE);
        });
        onIOThread(() -> {
            state = State.DISCONNECTED;
            if (telnetSession != null) {
                telnetSession.close();
                telnetSession = null;
                decoder = null;
                encoder = null;

            }
        });
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
            final ByteBuffer copy = ByteBuffer.allocate(buffer.remaining());
            copy.put(buffer);
            copy.flip();
            eventLoop.invokeLater(() -> {
                telnetSession.writeData(copy);
            });
        }
    }

    private static class PasteAction extends AbstractAction {
        private final InputDevice inputDevice;

        public PasteAction(InputDevice inputDevice) {
            this.inputDevice = inputDevice;
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
                    inputDevice.pasted(CharBuffer.wrap((CharSequence)contents.getTransferData(DataFlavor.stringFlavor)));
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

}
