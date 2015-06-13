package net.virtualinfinity.emulation;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.nio.CharBuffer;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public class KeyListenerInputDevice extends KeyAdapter implements InputDevice {
    private final Encoder encoder;
    public KeyListenerInputDevice(Encoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        typed(e.getKeyChar());
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
        encoder.character(CharBuffer.wrap("" + ch));
        encoder.flushCharacters();
        encoder.flush(false);
    }

    @Override
    public void right() {
        encoder.right();
        encoder.flush(false);
    }

    @Override
    public void left() {
        encoder.left();
        encoder.flush(false);
    }

    @Override
    public void down() {
        encoder.down();
        encoder.flush(false);
    }

    @Override
    public void up() {
        encoder.up();
        encoder.flush(false);
    }

    @Override
    public void sendStatusReport() {
        encoder.statusReport();
        encoder.flush(false);
    }

    @Override
    public void reportPosition(int currentLine, int currentColumn) {
        encoder.reportPosition(currentLine, currentColumn);
        encoder.flush(false);
    }
}
