package net.virtualinfinity.emulation.ui;

import net.virtualinfinity.emulation.Intensity;

import java.awt.*;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public class ColorConverter {
    public static Color color(net.virtualinfinity.emulation.Color color, Intensity intensity) {
        switch (intensity) {
            case FAINT:
                switch (color) {
                    case BLACK:   return new Color(0x000000);
                    case RED:     return new Color(0x770000);
                    case GREEN:   return new Color(0x007700);
                    case YELLOW:  return new Color(0x773B00);
                    case BLUE:    return new Color(0x000077);
                    case MAGENTA: return new Color(0x770077);
                    case CYAN:    return new Color(0x007777);
                    case WHITE:   return new Color(0x777777);
                }
                break;
            case NORMAL:
                switch (color) {
                    case BLACK:   return new Color(0x000000);
                    case RED:     return new Color(0xAA0000);
                    case GREEN:   return new Color(0x00AA00);
                    case YELLOW:  return new Color(0xAA5500);
                    case BLUE:    return new Color(0x0000AA);
                    case MAGENTA: return new Color(0xAA00AA);
                    case CYAN:    return new Color(0x00AAAA);
                    case WHITE:   return new Color(0xAAAAAA);
                }
                break;
            case BOLD:
                switch (color) {
                    case BLACK:   return new Color(0x555555);
                    case RED:     return new Color(0xFF5555);
                    case GREEN:   return new Color(0x55FF55);
                    case YELLOW:  return new Color(0xFFFF55);
                    case BLUE:    return new Color(0x5555FF);
                    case MAGENTA: return new Color(0xFF55FF);
                    case CYAN:    return new Color(0x55FFFF);
                    case WHITE:   return new Color(0xFFFFFF);
                }
                break;
        }
        return Color.black;
    }
}
