package net.virtualinfinity.emulation.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public class PresentationComponentLine implements PresentationLine, AttributedCharacterLine {
    private final PresentationComponentModel parent;
    private final List<Position> positions = new ArrayList<>();

    public PresentationComponentLine(PresentationComponentModel parent) {
        this.parent = parent;
    }

    public void deleteCharacters(int position, int count) {
        existingPositions(position, count).clear();
        parent.updated();
    }

    private int bound(int position) {
        return Math.max(0, Math.min(position, positions.size()));
    }

    public void setCodePoint(int column, int codePoint, DisplayAttribute attribute) {
        positions.addAll(Collections.nCopies(Math.max(0, column - positions.size() + 1), null));
        positions.set(column, new Position(codePoint, attribute));
        parent.updated();
    }

    @Override
    public void eraseAll() {
        positions.clear();
        parent.updated();
    }

    @Override
    public void eraseTo(int column) {
        eraseSection(0, column + 1);
    }

    private List<Position> existingPositions(int start, int count) {
        return positions.subList(bound(start), bound(start +count));
    }

    @Override
    public void eraseFrom(int column) {
        eraseSection(column, positions.size());
    }

    @Override
    public void eraseSection(int start, int count) {
        existingPositions(start, count).replaceAll(p -> null);
        parent.updated();
    }

    @Override
    public void insert(int start, int count) {
        if (positions.size() <= start) {
            return;
        }

        positions.addAll(start, Collections.nCopies(count, null));
        parent.updated();
    }

    public void scrollLeft(int count) {
        existingPositions(0, count).clear();
        parent.updated();
    }

    public void scrollRight(int count) {
        insert(0, count);
        parent.updated();
    }

    @Override
    public int numPositions() {
        return positions.size();
    }

    @Override
    public DisplayAttribute attribute(int col) {
        return read(col).attribute;
    }

    private Position read(int col) {
        final Position position = positions.size() > col ? positions.get(col) : null;
        if (position == null) {
            return new Position(0, null);
        }
        return position;
    }

    @Override
    public int codePoint(int col) {
        return read(col).codePoint;
    }


    private static class Position {
        final int codePoint;
        final DisplayAttribute attribute;

        public Position(int codePoint, DisplayAttribute attribute) {
            this.codePoint = codePoint == 0 ? 32 : codePoint;
            this.attribute = attribute;
        }

        public int codePoint() {
            return codePoint;
        }

        public DisplayAttribute attribute() {
            return attribute;
        }
    }

    @Override
    public String toString() {
        return positions.stream().mapToInt(value -> value == null ? ' ' : value.codePoint).collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
    }
}
