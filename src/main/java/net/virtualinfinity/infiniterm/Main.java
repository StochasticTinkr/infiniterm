package net.virtualinfinity.infiniterm;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public class Main {
    public static void main(String[] args) throws IOException {
        EventQueue.invokeLater(() -> {
            setLookAndFeel();
            try {
                final Terminal terminalFrame = new Terminal();
                terminalFrame.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    private static void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (final Exception ignore) {
            // Unable to choose system look and feel. Oh well.
        }
    }
}
