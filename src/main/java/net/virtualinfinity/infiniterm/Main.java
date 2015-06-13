package net.virtualinfinity.infiniterm;

import net.virtualinfinity.nio.EventLoop;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public class Main {
    public static void main(String[] args) throws IOException {
        final EventLoop eventLoop = new EventLoop();
        EventQueue.invokeLater(() -> {
            setLookAndFeel();
            final TerminalFrame terminalFrame = new TerminalFrame(eventLoop);
            terminalFrame.show();
        });
        eventLoop.run();

    }

    private static void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (final Exception ignore) {
            // Unable to choose system look and feel. Oh well.
        }
    }
}
