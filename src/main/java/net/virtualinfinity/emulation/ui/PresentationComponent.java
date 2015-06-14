package net.virtualinfinity.emulation.ui;

import net.virtualinfinity.emulation.Intensity;

import javax.swing.*;
import java.awt.*;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public class PresentationComponent extends JComponent implements Scrollable {
    private final PresentationComponentModel model = new PresentationComponentModel();
    private boolean debugFont;

    public PresentationComponent() {
//        Stream.of(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()).forEachOrdered(System.out::println);
        setBackground(Color.black);
        setForeground(Color.white);
        setFocusable(true);
        model.addPresentationComponentModelListener(() -> {
            revalidate();
            repaint();
        });
    }

    public PresentationDevice getModel() {
        return model;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        final Graphics2D g2d = (Graphics2D) g;
        g2d.setFont(getFont());
        g2d.setColor(getBackground());
        g2d.fillRect(0, 0, getWidth(), getHeight());
        final Shape oldClip = g2d.getClip();

        final int start = g2d.getFontMetrics(getFont()).getAscent();
        for (int pass = 0; pass < 2; ++pass) {
            int y = start;
            int lineNumber = 0;
            for (final AttributedCharacterLine lineModel: model.lines()) {
                for (int col = 0, x = 0; col < model.numColumns() && col < lineModel.numPositions(); ++col, x += getCharacterWidth(g2d)) {
                    final DisplayAttribute attribute = lineModel.attribute(col);
                    if (attribute != null) {
                        final int codePoint = lineModel.codePoint(col);
                        final Color fgColor = ColorConverter.color(fgColor(attribute), attribute.intensity());
                        final Color bgColor = ColorConverter.color(bgColor(attribute), Intensity.NORMAL);
                        switch (codePoint) {
                            case '░':
                                if (pass == 0) fillBackground(g2d, x, y, interpolate(fgColor, bgColor, .25f));
                                break;
                            case '▒':
                                if (pass == 0) fillBackground(g2d, x, y, interpolate(fgColor, bgColor, .50f));
                                break;
                            case '▓':
                                if (pass == 0) fillBackground(g2d, x, y, interpolate(fgColor, bgColor, .75f));
                                break;
                            case '█':
                                if (pass == 0) fillBackground(g2d, x, y, fgColor);
                                break;
                            default:
                                if (pass == 0) fillBackground(g2d, x, y, bgColor);
                                if (pass == 1) {
                                    if (codePoint != 0) {
                                        g2d.setColor(fgColor);
                                        final char[] data = Character.toChars(codePoint);
                                        g2d.drawChars(data, 0, data.length, x, y);
                                    }
                                }
                                break;
                        }
                    }
                }
                ++lineNumber;
                y += lineHeight();
            }
            final DisplayAttribute cursorAttribute = model.displayedLine(model.currentLine()).attribute(model.currentColumn());
            if (cursorAttribute != null) {
                g2d.setColor(ColorConverter.color(fgColor(cursorAttribute), cursorAttribute.intensity()));
            } else {
                g2d.setColor(ColorConverter.color(net.virtualinfinity.emulation.Color.WHITE, Intensity.NORMAL));
            }
            g2d.drawString("_", model.currentColumn() * getCharacterWidth(g2d), lineHeight() * (model.currentLine() + model.topLineIndex()) + start);
        }
        g2d.setClip(oldClip);
    }

    private Color interpolate(Color first, Color second, float v) {
        final float [] fv = first.getColorComponents(new float[3]);
        final float [] sv = second.getColorComponents(new float[3]);
        for (int i = 0; i < 3; ++i) {
            fv[i] = fv[i] * v + sv[i] * (1-v);
        }
        return new Color(fv[0], fv[1], fv[2]);
    }

    private void fillBackground(Graphics2D g2d, int x, int y, Color color) {
        g2d.setColor(color);
        final Rectangle s = new Rectangle(x, y - g2d.getFontMetrics().getAscent() + 1, getCharacterWidth(g2d), lineHeight()+1);
        g2d.fill(s);
        if (debugFont) {
            g2d.setColor(Color.YELLOW);
            g2d.draw(s);
        }
    }

    private int getCharacterWidth(Graphics2D g2d) {
        return g2d.getFontMetrics().getMaxAdvance()-1;
    }

    private net.virtualinfinity.emulation.Color bgColor(DisplayAttribute attribute) {
        return color(attribute, !attribute.inverseDisplay());
    }

    private net.virtualinfinity.emulation.Color fgColor(DisplayAttribute attribute) {
        return color(attribute, attribute.inverseDisplay());
    }

    private net.virtualinfinity.emulation.Color color(DisplayAttribute attribute, boolean background) {
        return background ? attribute.backgroundColor() : attribute.foregroundColor();
    }


    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return new Dimension(getFontMetrics(getFont()).getMaxAdvance()*model.numColumns(),  lineHeight() *model.numLines());
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(getFontMetrics(getFont()).getMaxAdvance()*model.numColumns(),  lineHeight() * Math.max(model.totalLines(), model.numLines()));
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredScrollableViewportSize();
    }


    public int lineHeight() {
        return getFontMetrics(getFont()).getAscent() + getFontMetrics(getFont()).getMaxDescent()-1;
    }


    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return lineHeight();
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return lineHeight() *8;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    @Override
    public void repaint() {
        repaint(0, 0, 0, getWidth(), getHeight());
    }
}
