package net.virtualinfinity.infiniterm.settings.ui;

import net.virtualinfinity.infiniterm.Terminal;
import net.virtualinfinity.infiniterm.settings.Connection;
import net.virtualinfinity.infiniterm.settings.ConnectionStore;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;
import java.util.prefs.BackingStoreException;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public class ConnectionSettings {
    private final JDialog frame;
    private final ConnectionStore connectionStore;
    private final ConnectionEdit editPane;
    private final DefaultListModel<Connection> connectionListModel = new DefaultListModel<>();
    private final JList<Connection> connectionList = new JList<>(connectionListModel);

    public ConnectionSettings(ConnectionStore connectionStore) throws BackingStoreException {
        this.frame = new JDialog((Frame)null, "Connection Settings");
        this.frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.connectionStore = connectionStore;
        editPane = new ConnectionEdit();
        connectionStore.getConnections().forEach(connectionListModel::addElement);
        connectionList.setPrototypeCellValue(new Connection().name("This is a really long name, but it will help."));
        connectionList.setCellRenderer(new ConnectionListCellRenderer());
        connectionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        connectionList.addListSelectionListener(e -> editPane.copyFromConnection(connectionList.getSelectedValue()));
        final JPopupMenu contextMenu = new JPopupMenu();
        connectionList.setComponentPopupMenu(contextMenu);

        frame.add(BorderLayout.WEST, new JScrollPane(connectionList));
        frame.add(BorderLayout.CENTER, editPane.getEditPane());

        final Action newAction = new AbstractAction("New") {
            @Override
            public void actionPerformed(ActionEvent e) {
                addNewConnection();
            }
        };

        final Action copyAction = new AbstractAction("Copy") {
            @Override
            public void actionPerformed(ActionEvent e) {
                final Connection selectedValue = connectionList.getSelectedValue();
                createCopy(selectedValue);

            }
        };

        contextMenu.add(newAction);
        contextMenu.add(copyAction);
        contextMenu.add(new AbstractAction("Delete") {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    connectionStore.deleteConnection(connectionList.getSelectedValue());
                    connectionListModel.removeElement(connectionList.getSelectedValue());
                } catch (final BackingStoreException ignore) {
                }
            }
        });


        final JPanel buttonPane = new JPanel();
        buttonPane.add(new JButton(newAction));
        buttonPane.add(new JButton(copyAction));

        buttonPane.add(new JSeparator(SwingConstants.VERTICAL));
        buttonPane.add(new JButton(new AbstractAction("Cancel") {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        }));
        buttonPane.add(new JButton(new AbstractAction("Save") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (connectionList.getSelectedIndex() < 0) {
                    final Connection newConnection = new Connection();
                    editPane.copyToConnection(newConnection);
                    createCopy(newConnection);
                }
                final Connection connection = connectionList.getSelectedValue();
                editPane.copyToConnection(connection);
                try {
                    connectionStore.saveConnection(connection);
                } catch (BackingStoreException e1) {
                    displayError(e1, "save your connection information");
                }
            }
        }));
        buttonPane.add(new JButton(new AbstractAction("Connect") {
            @Override
            public void actionPerformed(ActionEvent e) {
                connectToSelected();
            }
        }));
        frame.add(BorderLayout.SOUTH, buttonPane);

        connectionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    connectToSelected();
                }
            }
        });

        if (connectionListModel.isEmpty()) {
            addNewConnection();
        }
        connectionList.setSelectedIndex(0);

    }

    private void displayError(Exception exception, String action) {
        final StringWriter out = new StringWriter();
        out.write("<html><p>I encountered an unexpected error while trying to " + action + ". </p><pre>");
        final PrintWriter writer = new PrintWriter(out);
        exception.printStackTrace(writer);
        writer.flush();
        out.write("</pre></html>");
        out.flush();
        JOptionPane.showMessageDialog(frame, out.toString(), "Unable to save connection.", JOptionPane.ERROR_MESSAGE);
    }

    private void connectToSelected() {
        try {
            final Terminal terminal = new Terminal();
            terminal.showAndConnect(connectionList.getSelectedValue());
        } catch (IOException e) {
            displayError(e, "open the terminal");
        }
    }

    private void createCopy(Connection selectedValue) {
        addNewConnection();
        editPane.copyFromConnection(selectedValue);
    }

    private void addNewConnection() {
        final Connection connection = createNewConnection();
        editPane.copyFromConnection(null);
        editPane.copyToConnection(connection);
        connectionListModel.addElement(connection);
        connectionList.setSelectedValue(connection, true);
    }

    private Connection createNewConnection() {
        return new Connection().id(UUID.randomUUID().toString());
    }

    public void show() {
        frame.pack();
        frame.setVisible(true);
    }

    private static class ConnectionListCellRenderer implements ListCellRenderer<Connection> {
        private final ListCellRenderer<Object> delegate = new DefaultListCellRenderer();

        @Override
        public Component getListCellRendererComponent(JList<? extends Connection> list, Connection value, int index, boolean isSelected, boolean cellHasFocus) {
            return delegate.getListCellRendererComponent(list, value.name(), index, isSelected, cellHasFocus);
        }
    }
}
