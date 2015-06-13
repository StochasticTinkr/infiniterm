package net.virtualinfinity.emulation.ui;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public interface PresentationDevice {
    PresentationDevice currentLine(int currentLine);
    int currentLine();
    default PresentationDevice moveCurrentLine(int lines) {
        return currentLine(currentLine() + lines);
    }

    PresentationDevice currentColumn(int currentColumn);
    int currentColumn();
    default PresentationDevice moveCurrentColumn(int columns) {
        return currentColumn(currentColumn() + columns);
    }

    int numLines();
    PresentationDevice numLines(int numLines);

    int numColumns();
    PresentationDevice numColumns(int numColumns);

    PresentationLine displayedLine(int line);

    void writeCodePoint(int codePoint, DisplayAttribute attribute);
    void newline();

    void deleteCharacters(int count);
    void deleteLines(int count);

    void eraseTopToCursor();
    void eraseCursorToBottom();

    void erase();

    void insertLine(int count);

    void insertLine(int index, int count);

    void scrollDown(int count);

    void scrollLeft(int count);

    void scrollUp(int count);

    void scrollRight(int count);
}
