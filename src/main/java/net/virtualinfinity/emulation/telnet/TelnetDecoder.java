package net.virtualinfinity.emulation.telnet;

import net.virtualinfinity.emulation.AreaType;
import net.virtualinfinity.emulation.Decoder;
import net.virtualinfinity.telnet.TelnetSession;
import net.virtualinfinity.telnet.TelnetSessionListener;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public class TelnetDecoder implements TelnetSessionListener {
    private final Decoder decoder;

    public TelnetDecoder(Decoder decoder) {
        this.decoder = decoder;
    }

    @Override
    public void processData(ByteBuffer data, TelnetSession session) throws IOException {
        decoder.received(data);
    }

    @Override
    public void doBreak(TelnetSession session) {
    }

    @Override
    public void doInterrupt(TelnetSession session) {
    }

    @Override
    public void doAbortOutput(TelnetSession session) {
    }

    @Override
    public void doAreYouThere(TelnetSession session) {
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
    public void doGoAhead(TelnetSession session) {
    }
}
