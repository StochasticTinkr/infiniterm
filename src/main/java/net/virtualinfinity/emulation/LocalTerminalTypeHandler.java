package net.virtualinfinity.emulation;

import net.virtualinfinity.telnet.Option;
import net.virtualinfinity.telnet.SubNegotiationOutputChannel;
import net.virtualinfinity.telnet.option.handlers.OptionReceiver;
import net.virtualinfinity.telnet.option.handlers.SubNegotiationReceiver;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public class LocalTerminalTypeHandler implements OptionReceiver<Void>, SubNegotiationReceiver<Void> {
    private SubNegotiationOutputChannel options;

    public LocalTerminalTypeHandler(SubNegotiationOutputChannel options) {
        this.options = options;
    }

    @Override
    public Void enabledRemotely(Void handlerData) {
        return handlerData;
    }

    @Override
    public Void disabledRemotely(Void handlerData) {
        return handlerData;
    }

    @Override
    public Void enabledLocally(Void handlerData) {
        return handlerData;
    }

    @Override
    public Void disabledLocally(Void handlerData) {
        return handlerData;
    }

    @Override
    public Void startSubNegotiation(Void handlerData) {
        return null;
    }

    @Override
    public Void subNegotiationData(ByteBuffer data, Void handlerData) {
        if (data.hasRemaining() && data.get() == 1) {
            options.sendSubNegotiation(optionCode(), ByteBuffer.wrap("\u0000ANSI".getBytes(Charset.forName("US-ASCII"))));
        }
        return null;
    }

    @Override
    public Void endSubNegotiation(Void handlerData) {
        return null;
    }

    @Override
    public int optionCode() {
        return Option.TERMINAL_TYPE.optionCode();
    }


}
