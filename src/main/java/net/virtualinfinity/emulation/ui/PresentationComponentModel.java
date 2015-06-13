package net.virtualinfinity.emulation.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public class PresentationComponentModel implements PresentationDevice {
    private final Collection<PresentationComponentModelListener> listeners = new ArrayList<>();
    private final List<PresentationComponentLine> lines = new ArrayList<>();
    private int topLineIndex;
    private int currentLine;
    private int currentColumn;

    private int numLines = 25;
    private int numColumns = 80;

    public boolean addPresentationComponentModelListener(PresentationComponentModelListener presentationComponentModelListener) {
        return listeners.add(presentationComponentModelListener);
    }

    public boolean removePresentationComponentModelListener(PresentationComponentModelListener o) {
        return listeners.remove(o);
    }

    @Override
    public int currentLine() {
        return currentLine;
    }

    @Override
    public PresentationDevice currentLine(int currentLine) {
        this.currentLine = Math.max(0, Math.min(currentLine, numLines));

        return updated();
    }

    @Override
    public int currentColumn() {
        return currentColumn;
    }

    @Override
    public PresentationDevice currentColumn(int currentColumn) {
        this.currentColumn = Math.max(0, Math.min(currentColumn, numColumns - 1));

        return updated();
    }

    @Override
    public int numLines() {
        return numLines;
    }

    @Override
    public PresentationDevice numLines(int numLines) {
        this.numLines = numLines;

        return updated();
    }

    @Override
    public int numColumns() {
        return numColumns;
    }

    public PresentationDevice numColumns(int numColumns) {
        this.numColumns = numColumns;

        return updated();
    }

    @Override
    public PresentationComponentLine displayedLine(int line) {
        if (lines.size() <= actualIndex(line)) {
            while (lines.size() <= actualIndex(line)) {
                lines.add(new PresentationComponentLine(this));
            }
            updated();
        }
        return lines.get(actualIndex(line));
    }

    private int actualIndex(int line) {
        return topLineIndex + line;
    }

    @Override
    public void writeCodePoint(int codePoint, DisplayAttribute attribute) {
        displayedLine(currentLine()).setCodePoint(currentColumn(), codePoint, attribute);
        ++currentColumn;
        if (currentColumn == numColumns) {
            moveToNextLine();
        }
        updated();
    }

    @Override
    public void newline() {
        moveToNextLine();
        updated();
    }

    private void moveToNextLine() {
        currentColumn = 0;
        currentLine++;
        if (currentLine >= numLines) {
            currentLine = numLines-1;
            ++topLineIndex;
        }
    }

    @Override
    public void deleteCharacters(int count) {
        displayedLine(currentLine()).deleteCharacters(currentColumn(), count);
    }

    @Override
    public void deleteLines(int count) {
        existingDisplayedLines(currentLine(), count).clear();
        updated();
    }

    private List<PresentationComponentLine> existingDisplayedLines(int start, int count) {
        return lines.subList(bound(actualIndex(start)), bound(actualIndex(start + count)));
    }

    private int bound(int index) {
        return Math.max(0, Math.min(index, lines.size()));
    }

    @Override
    public void eraseTopToCursor() {
        existingDisplayedLines(0, currentLine()).forEach(PresentationLine::eraseAll);
        displayedLine(currentLine()).eraseTo(currentColumn());
    }

    @Override
    public void eraseCursorToBottom() {
        displayedLine(currentLine()).eraseFrom(currentColumn());
        existingDisplayedLines(currentLine() + 1, numLines()).forEach(PresentationLine::eraseAll);
    }

    @Override
    public void erase() {
        existingDisplayedLines(0, numLines()).forEach(PresentationLine::eraseAll);
    }

    @Override
    public void insertLine(int count) {
        insertLine(currentLine(), count);
    }

    @Override
    public void insertLine(int index, int count) {
        if (lines.size() <= actualIndex(index)) {
            return;
        }
        final List<PresentationComponentLine> newLines = new ArrayList<>(count);
        for (int i = 0; i < count; ++i) {
            newLines.add(new PresentationComponentLine(this));
        }

        if (lines.addAll(index, newLines)) {
            updated();
        }
    }

    @Override
    public void scrollDown(int count) {
        topLineIndex += count;
        updated();
    }

    @Override
    public void scrollLeft(int count) {
        existingDisplayedLines(0, numLines()).forEach(presentationLine -> presentationLine.scrollLeft(count));
    }

    @Override
    public void scrollUp(int count) {
        scrollDown(-count);
    }

    @Override
    public void scrollRight(int count) {
        existingDisplayedLines(0, numLines()).forEach(presentationLine -> presentationLine.scrollRight(count));
    }

    public List<AttributedCharacterLine> lines() {
        return Collections.unmodifiableList(lines);
    }

    public int totalLines() {
        return lines.size();
    }

    public PresentationComponentModel updated() {
        listeners.forEach(PresentationComponentModelListener::modelUpdated);
        return this;
    }

    public int topLineIndex() {
        return topLineIndex;
    }
}
