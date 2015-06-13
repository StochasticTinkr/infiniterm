package net.virtualinfinity.emulation.ui;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public interface AttributedCharacterLine {
    int numPositions();

    DisplayAttribute attribute(int col);

    int codePoint(int col);
}
