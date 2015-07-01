package net.virtualinfinity.infiniterm;

import net.virtualinfinity.emulation.Decoder;
import net.virtualinfinity.emulation.Encoder;
import net.virtualinfinity.emulation.InputDevice;
import net.virtualinfinity.emulation.KeyListenerInputDevice;
import net.virtualinfinity.emulation.telnet.TelnetDecoder;
import net.virtualinfinity.emulation.ui.OutputDeviceImpl;
import net.virtualinfinity.emulation.ui.PresentationComponent;
import net.virtualinfinity.infiniterm.settings.Connection;
import net.virtualinfinity.nio.*;
import net.virtualinfinity.swing.StickyBottomScrollPane;
import net.virtualinfinity.telnet.*;
import net.virtualinfinity.telnet.option.BinaryTransmission;
import net.virtualinfinity.telnet.option.NegotiateAboutWindowSize;
import net.virtualinfinity.telnet.option.TerminalType;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.Closeable;
import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
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
    private final JSpinner fontSizes = new JSpinner(new SpinnerNumberModel(16, 6, 96, 1));
    private final JFrame frame;
    private final JComboBox<String> hostInput;
    private final DefaultComboBoxModel<Charset> charsetsModel = new DefaultComboBoxModel<>();
    private final JComboBox<Charset> encodingInput = new JComboBox<>(charsetsModel);
    private final JComboBox<String> fonts;
    private final EventLoop eventLoop;
    private final KeyListenerInputDevice inputDevice = new KeyListenerInputDevice();
    private final Action connectAction = new AbstractAction("Connect") {
        @Override
        public void actionPerformed(ActionEvent e) {
            connect();
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
    private final ClientStarter clientStarter = new ClientStarter(new ConnectionInitiator(new AsynchronousAddressResolver(),
        () -> {
            final SocketChannel channel = SocketChannel.open();
            channel.setOption(StandardSocketOptions.SO_RCVBUF, 65536);
            channel.setOption(StandardSocketOptions.SO_SNDBUF, 65536);
            channel.setOption(StandardSocketOptions.TCP_NODELAY, true);
            channel.setOption(StandardSocketOptions.IP_TOS, 8);
            channel.setOption(StandardSocketOptions.IP_TOS, 8);
            return new SocketChannelWrapper(channel);
        }, ServerSocketChannelWrapper.PROVIDER), SessionStarters.client(65536, 65536) );
    private Closeable closer;

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
        fonts = new JComboBox<>(ge.getAvailableFontFamilyNames());
        Arrays.asList(Font.MONOSPACED, "Monoco", "PT Mono").forEach(fonts::setSelectedItem);
        fonts.setEditable(false);
        toolBar.add(fonts);
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
        charsetsModel.addElement(Charset.defaultCharset());
        if (Charset.isSupported(IBM_437)) {
            charsetsModel.addElement(Charset.forName(IBM_437));
        }
        Charset.availableCharsets().values().forEach(charsetsModel::addElement);
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
            final String hostName = StringUtils.substringBefore(selectedItem.toString(), ":");
            final int port = NumberUtils.toInt(StringUtils.substringAfter(selectedItem.toString(), ":"), 23);

            initiateConnection(new Connection().name(port == 23 ? hostName : hostName + ":" + port).hostName(hostName).port(port));
        });
    }

    private void initiateConnection(final Connection connection) {
        final Charset charset = selectedCharset();
        final OutputDeviceImpl device = new OutputDeviceImpl(view.getModel());
        device.inputDevice(inputDevice);
        decoder = new Decoder(device);
        onGuiThread(() -> frame.setTitle(TITLE_PREFIX + " - " + connection.name() + " (Resolving...)"));
        clientStarter.connect(eventLoop, connection.hostName(), connection.port(), new TelnetDecoder(decoder) {
            @Override
            public void connected(Session session) {
                onGuiThread(() -> {
                    frame.setTitle(TITLE_PREFIX + " - " + connection.name() + " (Connected)");
                    view.requestFocusInWindow();
                });
                connectionSuccessful(session, charset);
            }

            @Override
            public void connecting() {
                onGuiThread(() -> frame.setTitle(TITLE_PREFIX + " - " + connection.name() + " (Connecting...)"));
            }

            @Override
            public void connectionClosed() {
                disconnect();
            }

            @Override
            public void connectionFailed(IOException e) {
                disconnectOnError(e);
            }
        });
    }

    private void connectionSuccessful(Session session, Charset charset) {
        state = State.CONNECTED;
        closer = session::close;
        encoder = new Encoder(new TelnetSessionDispatcher(session.outputChannel(), eventLoop));
        onGuiThread(() -> inputDevice.setEncoder(encoder));
        setCharSet(charset);
        negotiateOptions(session);
    }

    private void negotiateOptions(Session session) {
        session.options().option(Option.ECHO).allowRemote();
        BinaryTransmission.on(session, null).allowLocal().allowRemote();
        session.options().option(Option.SUPPRESS_GO_AHEAD).requestRemoteEnable().allowLocal();
        TerminalType.of(session).allowLocal(Collections.singleton("ANSI")::iterator);
        NegotiateAboutWindowSize.on(session).startOffering(view.getModel().numColumns(), view.getModel().numLines());

//        options.installOptionReceiver(new LocalTerminalTypeHandler(negotiationChannel)).allowLocal();
    }

    private Charset selectedCharset() {
        return encodingInput.getItemAt(encodingInput.getSelectedIndex());
    }

    private void onIOThread(Runnable action) {
        eventLoop.invokeLater(action);
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

    public void disconnect() {
        onGuiThread(() -> {
            disconnectAction.setEnabled(false);
            hostInput.setEnabled(true);
            connectAction.setEnabled(true);
            frame.setTitle(DISCONNECTED_TITLE);
        });
        onIOThread(() -> {
            state = State.DISCONNECTED;

            if (closer != null) {
                try {
                    closer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                closer = null;
                decoder = null;
                encoder = null;

            }
        });
    }

    public void showAndConnect(Connection connection) {
        show();
        encodingInput.setSelectedItem(Charset.forName(connection.charset()));
        fonts.setSelectedItem(connection.font());
        fontSizes.setValue(connection.fontSize());
        initiateConnection(connection);

    }


    private enum State {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
    }

    private static class TelnetSessionDispatcher implements Consumer<ByteBuffer> {
        private final OutputChannel telnetSession;
        private final EventLoop eventLoop;

        public TelnetSessionDispatcher(OutputChannel telnetSession, EventLoop eventLoop) {
            this.telnetSession = telnetSession;
            this.eventLoop = eventLoop;
        }

        @Override
        public void accept(ByteBuffer buffer) {
            final ByteBuffer copy = ByteBuffer.allocate(buffer.remaining());
            copy.put(buffer);
            copy.flip();
            eventLoop.invokeLater(() -> {
                telnetSession.write(copy);
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
