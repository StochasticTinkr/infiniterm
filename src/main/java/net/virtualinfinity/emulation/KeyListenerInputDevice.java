package net.virtualinfinity.emulation;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.nio.CharBuffer;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public class KeyListenerInputDevice extends KeyAdapter implements InputDevice {
    private Encoder encoder;
    private String lineEnding = "\r";

    @Override
    public void keyTyped(KeyEvent e) {
        switch (e.getExtendedKeyCode()) {
            case KeyEvent.VK_ENTER:
                encoder.character(CharBuffer.wrap(lineEnding));
                encoder.flush(true);
                break;
            default:
                typed(e.getKeyChar());
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_KP_UP:
                up();
                e.consume();
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_KP_DOWN:
                down();
                e.consume();
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_KP_LEFT:
                left();
                e.consume();
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_KP_RIGHT:
                right();
                e.consume();
                break;
            default:
        }
    }

    @Override
    public void typed(char ch) {
        if (encoder == null) {
            return;
        }
        encoder.character(CharBuffer.wrap("" + ch));
        encoder.flushCharacters();
        encoder.flush(false);
    }

    @Override
    public void right() {
        if (encoder == null) {
            return;
        }
        encoder.right();
        encoder.flush(false);
    }

    @Override
    public void left() {
        if (encoder == null) {
            return;
        }
        encoder.left();
        encoder.flush(false);
    }

    @Override
    public void down() {
        if (encoder == null) {
            return;
        }
        encoder.down();
        encoder.flush(false);
    }

    @Override
    public void up() {
        if (encoder == null) {
            return;
        }
        encoder.up();
        encoder.flush(false);
    }

    @Override
    public void sendStatusReport() {
        if (encoder == null) {
            return;
        }
        encoder.statusReport();
        encoder.flush(false);
    }

    @Override
    public void reportPosition(int currentLine, int currentColumn) {
        if (encoder == null) {
            return;
        }
        encoder.reportPosition(currentLine, currentColumn);
        encoder.flush(false);
    }

    @Override
    public void pasted(String pasted) {
        if (encoder == null) {
            return;
        }

        encoder.character(CharBuffer.wrap(pasted.replaceAll("\r?\n", lineEnding)));
        encoder.flush(false);
    }

    @Override
    public void setEncoder(Encoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public Encoder getEncoder() {
        return encoder;
    }

    public String lineEnding() {
        return lineEnding;
    }

    public KeyListenerInputDevice lineEnding(String lineEnding) {
        this.lineEnding = lineEnding;

        return this;
    }
}
