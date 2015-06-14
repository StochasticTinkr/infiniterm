package net.virtualinfinity.emulation;

import java.nio.CharBuffer;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public interface InputDevice {
    void typed(char ch);

    void right();
    void left();
    void down();
    void up();

    void sendStatusReport();
    void reportPosition(int currentLine, int currentColumn);

    void setEncoder(Encoder encoder);
    Encoder getEncoder();
    void pasted(CharBuffer charBuffer);
}
