package net.virtualinfinity.emulation.telnet;

import net.virtualinfinity.emulation.AreaType;
import net.virtualinfinity.emulation.Decoder;
import net.virtualinfinity.telnet.SessionListener;

import java.nio.ByteBuffer;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public abstract class TelnetDecoder implements SessionListener {
    private final Decoder decoder;

    public TelnetDecoder(Decoder decoder) {
        this.decoder = decoder;
    }

    @Override
    public void incomingData(ByteBuffer data)  {
        decoder.received(data);
    }

    @Override
    public void doEraseCharacter() {
        decoder.device().eraseCharacter(1);
    }

    @Override
    public void doEraseLine() {
        decoder.device().eraseArea(AreaType.LINE);
    }
}
