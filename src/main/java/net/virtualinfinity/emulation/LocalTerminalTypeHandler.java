package net.virtualinfinity.emulation;

import net.virtualinfinity.telnet.Option;
import net.virtualinfinity.telnet.TelnetSession;
import net.virtualinfinity.telnet.option.handlers.OptionReceiver;
import net.virtualinfinity.telnet.option.handlers.SubNegotiationReceiver;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public class LocalTerminalTypeHandler implements OptionReceiver<Void>, SubNegotiationReceiver<Void> {
    @Override
    public Void enabledRemotely(TelnetSession session, Void handlerData) {
        return handlerData;
    }

    @Override
    public Void disabledRemotely(TelnetSession session, Void handlerData) {
        return handlerData;
    }

    @Override
    public Void enabledLocally(TelnetSession session, Void handlerData) {
        return handlerData;
    }

    @Override
    public Void disabledLocally(TelnetSession session, Void handlerData) {
        return handlerData;
    }

    @Override
    public Void startSubNegotiation(TelnetSession session, Void handlerData) {
        return null;
    }

    @Override
    public Void subNegotiationData(ByteBuffer data, TelnetSession session, Void handlerData) {
        if (data.hasRemaining() && data.get() == 1) {
            session.sendSubNegotiation(optionCode(), ByteBuffer.wrap("\u0000ANSI".getBytes(Charset.forName("US-ASCII"))));
        }
        return null;
    }

    @Override
    public Void endSubNegotiation(TelnetSession session, Void handlerData) {
        return null;
    }

    @Override
    public int optionCode() {
        return Option.TERMINAL_TYPE.optionCode();
    }


}
