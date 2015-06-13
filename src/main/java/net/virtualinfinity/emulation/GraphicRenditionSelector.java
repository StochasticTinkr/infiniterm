package net.virtualinfinity.emulation;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public class GraphicRenditionSelector {
    public OutputDevice dispatch(int value, OutputDevice device) {
        if (value >= 10 && value <= 20) {
            return device.useFont(value-10);
        }
        if (value >= 30 && value <= 37) {
            return device.setForeground(Color.values()[value-30]);
        }
        if (value >= 40 && value <= 47) {
            return device.setBackground(Color.values()[value-40]);
        }
        switch (value) {
            case 0: return device.clearRenditionAttributes();
            case 1: return device.intensity(Intensity.BOLD);
            case 2: return device.intensity(Intensity.FAINT);
            case 3: return device.italicized(true);
            case 4: return device.underline(Underline.SINGLE);
            case 5: return device.blinking(Blinking.SLOW);
            case 6: return device.blinking(Blinking.FAST);
            case 7: return device.inverseDisplay(true);
            case 8: return device.concealed(true);
            case 9: return device.strikeThrough(true);
            case 21: return device.underline(Underline.DOUBLE);
            case 22: return device.intensity(Intensity.NORMAL);
            case 23: return device.italicized(false);
            case 24: return device.underline(Underline.NONE);
            case 25: return device.blinking(Blinking.NONE);
            case 26: return device; // reserved
            case 27: return device.inverseDisplay(false);
            case 28: return device.concealed(false);
            case 29: return device.strikeThrough(false);
            // 30-37 set foreground color
            case 38: return device; // reserved
            case 39: return device.resetForeground();
            // 40-47 set background color
            case 48: return device; // reserved
            case 49: return device.resetBackground();
            case 50: return device; // reserved
            case 51: return device.framed(true);
            case 52: return device.encircled(true);
            case 53: return device.overlined(true);
            case 54: return device.framed(false).encircled(false);
            case 55: return device.overlined(false);
            // 56-59 reserved
            case 60:
                return device.ideogramUnderline(Underline.SINGLE);
            case 61:
                return device.ideogramUnderline(Underline.DOUBLE);
            case 62: return device.ideogramOverline(Underline.SINGLE);
            case 63: return device.ideogramOverline(Underline.DOUBLE);
            case 64: return device.ideogramStressMarking(true);
            case 65: return device.ideogramUnderline(Underline.NONE)
                                  .ideogramOverline(Underline.NONE)
                                  .ideogramStressMarking(false);
        }
        return device;
    }
}
