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
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Arrays;
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
    private JComboBox<Charset> encodingInput;
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
    private String connectionAddress;

    {
        synchronized (Terminal.class) {
            id = nextId++;
        }
    }

    public Terminal() throws IOException {
        this.eventLoop = new EventLoop(this::handleException);
        this.frame = new JFrame(DISCONNECTED_TITLE);
        view = new PresentationComponent();
        final StickyBottomScrollPane scrollPane = new StickyBottomScrollPane(view, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        frame.add(scrollPane);
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
        addConnectionWidgets(toolBar);
        toolBar.addSeparator();
        addEncodingWidgets(toolBar);
        toolBar.addSeparator();
        toolBar.add(new JLabel("Font:"));
        final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final JComboBox<String> fonts = new JComboBox<>(ge.getAvailableFontFamilyNames());
        Arrays.asList(Font.MONOSPACED, "Monoco", "PT Mono").forEach(fonts::setSelectedItem);
        fonts.setEditable(false);
        toolBar.add(fonts);
        final JSpinner fontSizes = new JSpinner(new SpinnerNumberModel(16, 6, 60, 1));
        toolBar.add(fontSizes);
        final Runnable updateFont = () -> {
            view.setFont(new Font(fonts.getItemAt(fonts.getSelectedIndex()), Font.PLAIN, (Integer) fontSizes.getValue()));
            frame.pack();
        };
        fonts.addActionListener(e -> updateFont.run());
        fontSizes.addChangeListener(e -> updateFont.run());
        updateFont.run();
        frame.add(toolBar, BorderLayout.PAGE_START);
        view.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "paste");
        view.getActionMap().put("paste", new PasteAction(inputDevice));
        view.addKeyListener(inputDevice);
    }

    private void addEncodingWidgets(JToolBar toolBar) {
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
        encodingInput.setSelectedItem(Charset.forName(IBM_437));
        encodingInput.setAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onIOThread(() -> setCharSet(selectedCharset()));
            }
        });

        toolBar.add(encodingInput);

    }

    private void addConnectionWidgets(JToolBar toolBar) {
        toolBar.add(hostInput);
        toolBar.add(new JButton(connectAction));
        toolBar.add(new JButton(disconnectAction));
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
            hostInput.setEnabled(false);
            connectAction.setEnabled(false);
            onIOThread(() -> {
                onGuiThread(() -> frame.setTitle(TITLE_PREFIX + " - " + selectedItem.toString() + " (Resolving...)"));
                final InetSocketAddress address = resolveAddress(selectedItem.toString());
                connectionAddress = addressName(address);
                state = State.CONNECTING;
                onGuiThread(() -> frame.setTitle(TITLE_PREFIX + " - " + connectionAddress + " (Connecting...)"));
                SocketChannel channel = null;
                try {
                    channel = SocketChannel.open();
                    channel.configureBlocking(false);
                    channel.connect(address);
                    finishConnect(selectedCharset(), channel);
                } catch (Exception e) {
                    disconnectOnError(e);
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

    private void connectionSuccessful() {
        state = State.CONNECTED;
        frame.setTitle(TITLE_PREFIX + " - " + connectionAddress + " (Connected)");
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

    private void disconnectOnError(Exception e) {
        disconnect();
        onGuiThread(() ->
            JOptionPane.showMessageDialog(frame,
                "Unable to connect to host:" + e.toString(),
                "Connection error.", JOptionPane.ERROR_MESSAGE));
    }

    private TelnetSession createTelnetSession(SocketChannel channel, Decoder decoder) throws ClosedChannelException {
        final TelnetSession session = new TelnetSession(channel, new TelnetDecoder(decoder, this::disconnect, this::connectionSuccessful, this::disconnectOnError));
        eventLoop.registerHandler(channel, session);
        session.option(Option.ECHO).allowRemote();
        session.option(Option.BINARY_TRANSMISSION).allowRemote().allowLocal();
        session.option(Option.SUPPRESS_GO_AHEAD).requestRemoteEnable().allowLocal();
        session.installOptionReceiver(new LocalTerminalTypeHandler()).allowLocal();
        return session;
    }

    private InetSocketAddress resolveAddress(String host) {
        final int colon = host.indexOf(':');
        if (colon < 0) {
            return new InetSocketAddress(host, 23);
        }

        return new InetSocketAddress(host.substring(0, colon), Integer.parseInt(host.substring(colon + 1)));
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
                    inputDevice.pasted(((String)contents.getTransferData(DataFlavor.stringFlavor)));
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

}
