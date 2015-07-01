package net.virtualinfinity.infiniterm;

import net.virtualinfinity.infiniterm.settings.ConnectionStore;
import net.virtualinfinity.infiniterm.settings.ui.ConnectionSettings;
import net.virtualinfinity.nio.SocketSelectionActions;
import net.virtualinfinity.swing.StickyBottomScrollPane;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.*;
import org.apache.log4j.spi.LoggingEvent;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.text.SimpleAttributeSet;
import java.awt.*;
import java.io.IOException;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public class Main {
    public static void main(String[] args) throws IOException {
        LogManager.getLoggerRepository().setThreshold(Level.DEBUG);
        LogManager.getLogger(SocketSelectionActions.class).setLevel(Level.WARN);
        final SwingDocumentAppender swingAppender = new SwingDocumentAppender();
        LogManager.getLoggerRepository().getRootLogger().addAppender(swingAppender);

        EventQueue.invokeLater(() -> {
            setLookAndFeel();

            try {
                final JFrame logFrame = new JFrame("Log");
                final JTextArea view = new JTextArea(swingAppender.document());
                logFrame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
                view.setLineWrap(true);
                view.setColumns(80);
                view.setRows(15);
                view.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
                view.setEditable(false);
                logFrame.add(new StickyBottomScrollPane(view));
                logFrame.pack();
                logFrame.setVisible(true);
//                final Terminal terminalFrame = new Terminal();
//                terminalFrame.show();
                final ConnectionSettings connectionSettings = new ConnectionSettings(new ConnectionStore());
                connectionSettings.show();
            } catch (Exception e) {
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

    private static class SwingDocumentAppender extends AppenderSkeleton {
        private final Document document = new PlainDocument();
        private final StringBuilder builder = new StringBuilder();

        public SwingDocumentAppender() {
            setLayout(new TTCCLayout());
        }

        @Override
        protected void append(LoggingEvent event) {
            final String format = getLayout().format(event);
            synchronized (builder) {
                builder.append(format);
            }
            EventQueue.invokeLater(() -> {
                    if (document != null) {
                        try {
                            final String result;
                            synchronized (builder) {
                                result = builder.toString();
                                builder.delete(0, builder.length());
                            }
                            document.insertString(document.getLength(), result, SimpleAttributeSet.EMPTY);
                        } catch (BadLocationException e) {
                        }
                    }
                }
            );
        }

        public Document document() {
            return document;
        }

        @Override
        public void close() {
        }

        @Override
        public boolean requiresLayout() {
            return true;
        }
    }
}
