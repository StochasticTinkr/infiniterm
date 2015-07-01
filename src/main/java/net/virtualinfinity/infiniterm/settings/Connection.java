package net.virtualinfinity.infiniterm.settings;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public class Connection {
    private String id;
    private String name;
    private String hostName;
    private int port = 23;
    private String charset;
    private String font;
    private int fontSize;


    public String hostName() {
        return hostName;
    }

    public Connection hostName(String hostName) {
        this.hostName = hostName;

        return this;
    }

    public int port() {
        return port;
    }

    public Connection port(int port) {
        this.port = port;

        return this;
    }

    public String charset() {
        return charset;
    }

    public Connection charset(String charset) {
        this.charset = charset;

        return this;
    }

    public String name() {
        return name;
    }

    public Connection name(String name) {
        this.name = name;

        return this;
    }

    public String font() {
        return font;
    }

    public Connection font(String font) {
        this.font = font;

        return this;
    }

    public String id() {
        return id;
    }

    public Connection id(String id) {
        this.id = id;

        return this;
    }

    public int fontSize() {
        return fontSize;
    }

    public Connection fontSize(int fontSize) {
        this.fontSize = fontSize;

        return this;
    }
}
