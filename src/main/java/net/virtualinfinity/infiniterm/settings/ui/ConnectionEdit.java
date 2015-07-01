package net.virtualinfinity.infiniterm.settings.ui;

import net.virtualinfinity.infiniterm.settings.Connection;
import net.virtualinfinity.infiniterm.settings.ConnectionStore;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.UUID;
import java.util.Vector;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public class ConnectionEdit {
    private static final int DEFAULT_FONT_SIZE = 16;
    private static final int DEFAULT_PORT = 23;
    private static final String DEFAULT_NAME = "Untitled";
    private final JPanel editPane = new JPanel();
    private final JTextField nameField = new JTextField();
    private final JSpinner portNumberField = new JSpinner(new SpinnerNumberModel(DEFAULT_PORT, 1, 65536, 1));
    private final JTextField hostNameField = new JTextField();
    private final JComboBox<String> fontField = new JComboBox<>();
    private final JSpinner fontSizeField = new JSpinner(new SpinnerNumberModel(DEFAULT_FONT_SIZE, 6, 96, 2));
    private final JComboBox<String> charsetField = new JComboBox<>();

    public ConnectionEdit() {
        editPane.setLayout(new GridLayout(6, 2));
        fontField.setModel(new DefaultComboBoxModel<>(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        fontField.setEditable(false);
        charsetField.setModel(new DefaultComboBoxModel<>(new Vector<>(Charset.availableCharsets().keySet())));
        charsetField.setEditable(false);
        editPane.add(new JLabel("Name:"));
        editPane.add(nameField);
        editPane.add(new JLabel("Host:"));
        editPane.add(hostNameField);
        editPane.add(new JLabel("Port:"));
        editPane.add(portNumberField);
        editPane.add(new JLabel("Font:"));
        editPane.add(fontField);
        editPane.add(new JLabel("Font Size:"));
        editPane.add(fontSizeField);
        editPane.add(new JLabel("Encoding"));
        editPane.add(charsetField);
        clearFields();
    }

    public void copyToConnection(Connection connection) {
        connection
            .name(value(nameField))
            .hostName(value(hostNameField))
            .port(value(portNumberField))
            .font(value(fontField))
            .fontSize(value(fontSizeField))
            .charset(value(charsetField));
    }

    private String value(JComboBox<String> field) {
        return (String) field.getSelectedItem();
    }

    private int value(JSpinner field) {
        return ((Number) field.getValue()).intValue();
    }

    private String value(JTextField field) {
        return field.getText();
    }

    private void clearFields() {
        // Sets the last one that matches.
        Arrays.asList(Font.MONOSPACED, "Monoco", "PT Mono").forEach(fontField::setSelectedItem);
        charsetField.setSelectedItem("IBM437");
        set(fontSizeField, DEFAULT_FONT_SIZE);
        set(portNumberField, DEFAULT_PORT);
        set(hostNameField, "");
        set(nameField, DEFAULT_NAME);
    }

    public void copyFromConnection(Connection connection) {
        if (connection != null) {
            updateFields(connection);
        } else {
            clearFields();
        }
    }

    private void updateFields(Connection connection) {
        set(nameField,       connection.name());
        set(hostNameField,   connection.hostName());
        set(portNumberField, connection.port());
        set(fontField,       connection.font());
        set(fontSizeField,   connection.fontSize());
        set(charsetField,    connection.charset());
    }

    private void set(JComboBox<String> field, String value) {
        field.setSelectedItem(value);
    }

    private void set(JSpinner field, int value) {
        field.setValue(value);
    }

    private void set(JTextField field, String value) {
        field.setText(value);
    }

    public JComponent getEditPane() {
        return editPane;
    }
}
