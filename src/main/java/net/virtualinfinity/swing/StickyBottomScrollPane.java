package net.virtualinfinity.swing;

import javax.swing.*;
import java.awt.*;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public class StickyBottomScrollPane extends JScrollPane {
    public StickyBottomScrollPane(Component view, int vsbPolicy, int hsbPolicy) {
        super(view, vsbPolicy, hsbPolicy);
    }

    public StickyBottomScrollPane(Component view) {
        super(view);
    }

    public StickyBottomScrollPane(int vsbPolicy, int hsbPolicy) {
        super(vsbPolicy, hsbPolicy);
    }

    public StickyBottomScrollPane() {
    }

    /**
     * Sticks to the bottom, but doesn't force you to the bottom.
     */
    @Override
    public void validate() {
        final JScrollBar sb = getVerticalScrollBar();

        final int distanceToBottom = sb.getMaximum() - (sb.getValue() + sb.getVisibleAmount());

        super.validate();

        if (0 == distanceToBottom) {
            sb.setValue(sb.getMaximum());
        }
    }
}
