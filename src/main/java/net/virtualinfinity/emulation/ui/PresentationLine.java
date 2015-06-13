package net.virtualinfinity.emulation.ui;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public interface PresentationLine {

    void eraseAll();
    void eraseTo(int column);
    void eraseFrom(int column);
    void eraseSection(int start, int count);
    void insert(int start, int count);

}
