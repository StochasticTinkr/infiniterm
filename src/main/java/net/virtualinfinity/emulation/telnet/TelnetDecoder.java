package net.virtualinfinity.emulation.telnet;

import net.virtualinfinity.emulation.AreaType;
import net.virtualinfinity.emulation.Decoder;
import net.virtualinfinity.telnet.TelnetSession;
import net.virtualinfinity.telnet.TelnetSessionListener;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public class TelnetDecoder implements TelnetSessionListener {
    private final Decoder decoder;
    private final Runnable onClose;
    private final Runnable onConnect;
    private final Consumer<IOException> onConnectFailure;

    public TelnetDecoder(Decoder decoder, Runnable onClose, Runnable onConnect, Consumer<IOException> onConnectFailure) {
        this.decoder = decoder;
        this.onClose = onClose;
        this.onConnect = onConnect;
        this.onConnectFailure = onConnectFailure;
    }

    @Override
    public void processData(ByteBuffer data, TelnetSession session) throws IOException {
        decoder.received(data);
    }

    @Override
    public void doEraseCharacter(TelnetSession session) {
        decoder.device().eraseCharacter(1);
    }

    @Override
    public void doEraseLine(TelnetSession session) {
        decoder.device().eraseArea(AreaType.LINE);
    }

    @Override
    public void connectionClosed(TelnetSession session) {
        onClose.run();
    }

    @Override
    public void connected(TelnetSession session) {
        onConnect.run();
    }

    @Override
    public void connectionFailed(TelnetSession session, IOException e) {
        onConnectFailure.accept(e);
    }
}
