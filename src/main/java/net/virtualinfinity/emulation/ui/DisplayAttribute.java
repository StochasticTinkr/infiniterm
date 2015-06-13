package net.virtualinfinity.emulation.ui;

import net.virtualinfinity.emulation.Blinking;
import net.virtualinfinity.emulation.Color;
import net.virtualinfinity.emulation.Intensity;
import net.virtualinfinity.emulation.Underline;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public class DisplayAttribute implements Cloneable {
    private Color backgroundColor = Color.BLACK;
    private Color foregroundColor = Color.WHITE;
    private Intensity intensity = Intensity.NORMAL;
    private boolean italicized = false;
    private Underline underline = Underline.NONE;
    private Blinking blinking = Blinking.NONE;
    private boolean inverseDisplay = false;
    private boolean concealed = false;
    private int fontIndex = 0;
    private boolean strikeThrough = false;
    private boolean framed = false;
    private boolean encircled = false;
    private boolean overlined = false;
    private Underline ideogramUnderline = Underline.NONE;
    private Underline ideogramOverline = Underline.NONE;
    private boolean ideogramStressed = false;


    public Color backgroundColor() {
        return backgroundColor;
    }

    public DisplayAttribute backgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;

        return this;
    }

    public Color foregroundColor() {
        return foregroundColor;
    }

    public DisplayAttribute foregroundColor(Color foregroundColor) {
        this.foregroundColor = foregroundColor;

        return this;
    }

    public Intensity intensity() {
        return intensity;
    }

    public DisplayAttribute intensity(Intensity intensity) {
        this.intensity = intensity;

        return this;
    }

    public boolean italicized() {
        return italicized;
    }

    public DisplayAttribute italicized(boolean italicized) {
        this.italicized = italicized;

        return this;
    }

    public Underline underline() {
        return underline;
    }

    public DisplayAttribute underline(Underline underline) {
        this.underline = underline;

        return this;
    }

    public Blinking blinking() {
        return blinking;
    }

    public DisplayAttribute blinking(Blinking blinking) {
        this.blinking = blinking;

        return this;
    }

    public boolean inverseDisplay() {
        return inverseDisplay;
    }

    public DisplayAttribute inverseDisplay(boolean inverseDisplay) {
        this.inverseDisplay = inverseDisplay;

        return this;
    }

    public boolean concealed() {
        return concealed;
    }

    public DisplayAttribute concealed(boolean concealed) {
        this.concealed = concealed;

        return this;
    }

    public int fontIndex() {
        return fontIndex;
    }

    public DisplayAttribute fontIndex(int fontIndex) {
        this.fontIndex = fontIndex;

        return this;
    }

    public boolean strikeThrough() {
        return strikeThrough;
    }

    public DisplayAttribute strikeThrough(boolean strikeThrough) {
        this.strikeThrough = strikeThrough;

        return this;
    }

    public boolean framed() {
        return framed;
    }

    public DisplayAttribute framed(boolean framed) {
        this.framed = framed;

        return this;
    }

    public boolean encircled() {
        return encircled;
    }

    public DisplayAttribute encircled(boolean encircled) {
        this.encircled = encircled;

        return this;
    }

    public boolean overlined() {
        return overlined;
    }

    public DisplayAttribute overlined(boolean overlined) {
        this.overlined = overlined;

        return this;
    }

    public Underline ideogramUnderline() {
        return ideogramUnderline;
    }

    public DisplayAttribute ideogramUnderline(Underline ideogramUnderline) {
        this.ideogramUnderline = ideogramUnderline;

        return this;
    }

    public Underline ideogramOverline() {
        return ideogramOverline;
    }

    public DisplayAttribute ideogramOverline(Underline getIdeogramOverline) {
        this.ideogramOverline = getIdeogramOverline;

        return this;
    }

    public boolean ideogramStressed() {
        return ideogramStressed;
    }

    public DisplayAttribute ideogramStressed(boolean ideogramStressed) {
        this.ideogramStressed = ideogramStressed;

        return this;
    }

    public DisplayAttribute clone() {
        try {
            //noinspection CastToConcreteClass
            return (DisplayAttribute) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
