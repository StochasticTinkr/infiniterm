package net.virtualinfinity.infiniterm.settings;

import java.awt.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public class ConnectionStore {
    private static final String CHARSET = "charset";
    private static final String HOSTNAME = "hostname";
    private static final String PORT = "port";
    private static final String FONT = "font";
    private static final String NAME = "name";
    private static final String FONT_SIZE = "fontSize";
    final Preferences preferences = Preferences.userRoot().node("net/virtualinfinity/infiniterm");

    public List<Connection> getConnections() throws BackingStoreException {
        final List<Connection> connections = new ArrayList<>();
        for (final String id : connectionNames()) {
            final Preferences connectionPref = connectionPreference(id);

            final Connection connection =
                new Connection()
                    .charset(connectionPref.get(CHARSET, Charset.defaultCharset().name()))
                    .hostName(connectionPref.get(HOSTNAME, "example.com"))
                    .port(connectionPref.getInt(PORT, 23))
                    .font(connectionPref.get(FONT, Font.MONOSPACED))
                    .name(connectionPref.get(NAME, null))
                    .fontSize(connectionPref.getInt(FONT_SIZE, 16))
                    .id(id);


            connections.add(connection);
        }
        return connections;
    }

    private String[] connectionNames() throws BackingStoreException {
        return connectionsPreferences().childrenNames();
    }

    public void saveConnection(Connection connection) throws BackingStoreException {
        final Preferences connectionPref = connectionPreference(connection.id());
        connectionPref.clear();
        connectionPref.put(HOSTNAME, connection.hostName());
        connectionPref.putInt(PORT, connection.port());
        connectionPref.put(CHARSET, connection.charset());
        connectionPref.put(FONT, connection.font());
        connectionPref.put(NAME, connection.name());

    }

    public void deleteConnection(Connection connection) throws BackingStoreException {
        connectionPreference(connection.id()).removeNode();
    }

    private Preferences connectionPreference(String name) {
        return connectionsPreferences().node(name);
    }

    private Preferences connectionsPreferences() {
        return preferences.node("connections");
    }

    private static <T, R>  Function<T, R> safe(Function<T, R> forName) {
        return s -> { try { return forName.apply(s); } catch (Exception e) { return null; } };
    }

}
