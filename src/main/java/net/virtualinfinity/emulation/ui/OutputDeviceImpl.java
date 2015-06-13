package net.virtualinfinity.emulation.ui;

import net.virtualinfinity.emulation.*;
import net.virtualinfinity.emulation.Color;

import java.awt.*;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public class OutputDeviceImpl implements OutputDevice {
    private final PresentationDevice model;
    private InputDevice inputDevice;
    private DisplayAttribute currentAttribute = new DisplayAttribute();
    private int previousCodePoint;

    public OutputDeviceImpl(PresentationDevice model) {
        this.model = model;
    }

    @Override
    public OutputDevice received(CharSequence chars) {
        writeCodePoints(chars.codePoints().toArray());
        return this;
    }

    private void writeCodePoints(final int[] codePoints) {
        final DisplayAttribute currentAttribute = this.currentAttribute;
        updateModel(model -> {
            for (final int codePoint : codePoints) {
                model.writeCodePoint(codePoint, currentAttribute);
            }
        });
        if (codePoints.length != 0) {
            previousCodePoint = codePoints[codePoints.length-1];
        }
    }

    protected OutputDevice updateModel(Consumer<PresentationDevice> updater) {
        EventQueue.invokeLater(() -> updater.accept(model()));
        return this;
    }

    protected OutputDevice updateModelCount(ObjIntConsumer<PresentationDevice> updater, int value) {
        return updateModel(presentationDevice -> updater.accept(presentationDevice, value));
    }

    private PresentationDevice model() {
        return model;
    }

    @Override
    public OutputDevice intensity(Intensity intensity) {
        cloneAttribute().intensity(intensity);
        return this;
    }

    @Override
    public OutputDevice italicized(boolean italicized) {
        cloneAttribute().italicized(italicized);
        return this;
    }

    @Override
    public OutputDevice underline(Underline underline) {
        cloneAttribute().underline(underline);
        return this;
    }

    @Override
    public OutputDevice clearRenditionAttributes() {
        currentAttribute = new DisplayAttribute();
        return this;
    }

    @Override
    public OutputDevice blinking(Blinking blinking) {
        cloneAttribute().blinking(blinking);
        return this;
    }

    @Override
    public OutputDevice inverseDisplay(boolean inverse) {
        cloneAttribute().inverseDisplay(inverse);
        return this;
    }

    @Override
    public OutputDevice concealed(boolean concealed) {
        cloneAttribute().concealed(concealed);
        return this;
    }

    @Override
    public OutputDevice useFont(int fontIndex) {
        cloneAttribute().fontIndex(fontIndex);
        return this;
    }

    @Override
    public OutputDevice strikeThrough(boolean strikeThrough) {
        cloneAttribute().strikeThrough(strikeThrough);
        return this;
    }

    @Override
    public OutputDevice setForeground(Color color) {
        cloneAttribute().foregroundColor(color);
        return this;
    }

    @Override
    public OutputDevice setBackground(Color color) {
        cloneAttribute().backgroundColor(color);
        return this;
    }

    @Override
    public OutputDevice resetForeground() {
        cloneAttribute().foregroundColor(Color.WHITE);
        return this;
    }

    @Override
    public OutputDevice resetBackground() {
        cloneAttribute().backgroundColor(Color.BLACK);
        return this;
    }

    @Override
    public OutputDevice framed(boolean framed) {
        cloneAttribute().framed(framed);
        return this;
    }

    @Override
    public OutputDevice encircled(boolean encircled) {
        cloneAttribute().encircled(encircled);
        return this;
    }

    @Override
    public OutputDevice overlined(boolean overlined) {
        cloneAttribute().overlined(overlined);
        return this;
    }

    @Override
    public OutputDevice ideogramUnderline(Underline underline) {
        cloneAttribute().ideogramUnderline(underline);
        return this;
    }

    @Override
    public OutputDevice ideogramOverline(Underline underline) {
        cloneAttribute().ideogramOverline(underline);
        return this;
    }

    @Override
    public OutputDevice ideogramStressMarking(boolean stressMarking) {
        cloneAttribute().ideogramStressed(stressMarking);

        return this;
    }

    @Override
    public OutputDevice cursorLeft(int count) {
        return cursorRight(-count);
    }

    @Override
    public OutputDevice cursorDown(int count) {
        updateModelCount(PresentationDevice::moveCurrentLine, count);
        return this;
    }

    @Override
    public OutputDevice cursorRight(int count) {
        updateModelCount(PresentationDevice::moveCurrentColumn, count);
        return this;
    }

    @Override
    public OutputDevice cursorUp(int count) {
        return cursorDown(-count);
    }

    @Override
    public OutputDevice bell() {
        return this;
    }

    @Override
    public OutputDevice backspace() {
        return cursorLeft(1);
    }

    @Override
    public OutputDevice acknowledge(boolean positive) {
        return this;
    }

    @Override
    public OutputDevice nul() {
        return this;
    }

    @Override
    public OutputDevice startOfHeading() {
        return this;
    }

    @Override
    public OutputDevice cancel() {
        return this;
    }

    @Override
    public OutputDevice carriageReturn() {
        updateModelCount(PresentationDevice::moveCurrentColumn, 0);
        return this;
    }

    @Override
    public OutputDevice enableManualInput(boolean enabled) {
        return this;
    }

    @Override
    public OutputDevice formFeed() {
        return this;
    }

    @Override
    public OutputDevice horizontalTab() {
        return this;
    }

    @Override
    public OutputDevice lineFeed() {
        return nextLine();
    }

    @Override
    public OutputDevice startOfText() {
        return this;
    }

    @Override
    public OutputDevice verticalTab() {
        return this;
    }

    @Override
    public OutputDevice breakPermittedHere(boolean isPermitted) {
        return this;
    }

    @Override
    public OutputDevice cancelCharacter() {
        return this;
    }

    @Override
    public OutputDevice markHorizontalTab() {
        return this;
    }

    @Override
    public OutputDevice cursorPreviousLine(int lines) {
        return cursorNextLine(-lines);
    }

    @Override
    public OutputDevice nextLine() {
        updateModel(PresentationDevice::newline);
        return this;
    }

    @Override
    public OutputDevice cursorNextLine(int lines) {
        return cursorDown(lines).cursorCharacterPosition(1);
    }

    @Override
    public OutputDevice partialLineForward() {
        return this;
    }

    @Override
    public OutputDevice partialLineBackward() {
        return this;
    }

    @Override
    public OutputDevice reverseLineFeed() {
        return cursorPreviousLine(1);
    }

    @Override
    public OutputDevice markVerticalTab() {
        return this;
    }

    @Override
    public OutputDevice interrupt() {
        return this;
    }

    @Override
    public OutputDevice reset() {
        return clearRenditionAttributes().eraseArea(AreaType.PAGE);
    }

    @Override
    public OutputDevice cursorCharacterPosition(int characterPosition) {
        return updateModelCount(PresentationDevice::currentColumn, characterPosition - 1);
    }

    @Override
    public OutputDevice line(int line) {
        return updateModelCount(PresentationDevice::currentLine, line - 1);
    }

    @Override
    public OutputDevice delete(int count) {
        return updateModelCount(PresentationDevice::deleteCharacters, count);
    }

    @Override
    public OutputDevice deleteLine(int count) {
        return updateModelCount(PresentationDevice::deleteLines, count);
    }

    @Override
    public OutputDevice setDimensions(int lineDimension, int positionDimension) {
        return this;
    }

    @Override
    public OutputDevice eraseInAreaToCursor(AreaType type) {
        updateModel(model -> {
            switch (type) {
                case PAGE:
                    model.eraseTopToCursor();
                    break;
                case AREA:
                    break;
                case LINE:
                    model.displayedLine(model.currentLine()).eraseTo(model.currentColumn());
                    break;
                case FIELD:
                    break;
            }
        });
        return this;
    }

    @Override
    public OutputDevice eraseInAreaAfterCursor(AreaType type) {
        updateModel(model -> {
            switch (type) {
                case PAGE:
                    model.eraseCursorToBottom();
                    break;
                case AREA:
                    break;
                case LINE:
                    model.displayedLine(model.currentLine()).eraseFrom(model.currentColumn());
                    break;
                case FIELD:
                    break;
            }
        });
        return this;
    }

    @Override
    public OutputDevice eraseArea(AreaType type) {
        updateModel(model -> {
            switch (type) {
                case PAGE:
                    model.erase();
                    break;
                case AREA:
                    break;
                case LINE:
                    model.displayedLine(model.currentLine()).eraseAll();
                    break;
                case FIELD:
                    break;
            }
        });
        return this;
    }

    @Override
    public OutputDevice eraseCharacter(int count) {
        updateModel(model -> model.displayedLine(model.currentLine()).eraseSection(model.currentColumn(), count));
        return this;
    }

    @Override
    public OutputDevice setFont(int fontIndex, int fontId) {
        return this;
    }

    @Override
    public OutputDevice insertCharacter(int count) {
        updateModel(model -> model.displayedLine(model.currentLine()).insert(model.currentColumn(), count));
        return this;
    }

    @Override
    public OutputDevice insertLine(int count) {
        updateModelCount(PresentationDevice::insertLine, count);
        return this;
    }

    @Override
    public OutputDevice repeatPreviousCharacter(int count) {
        final int[] codePoints = new int[count];
        Arrays.fill(codePoints, previousCodePoint);
        writeCodePoints(codePoints);
        return this;
    }

    @Override
    public OutputDevice scrollDown(int count) {
        updateModelCount(PresentationDevice::scrollDown, count);
        return this;
    }

    @Override
    public OutputDevice scrollLeft(int count) {
        updateModelCount(PresentationDevice::scrollLeft, count);
        return this;
    }

    @Override
    public OutputDevice scrollRight(int count) {
        updateModelCount(PresentationDevice::scrollRight, count);
        return this;
    }

    @Override
    public OutputDevice scrollUp(int count) {
        updateModelCount(PresentationDevice::scrollUp, count);
        return this;
    }

    private DisplayAttribute cloneAttribute() {
        currentAttribute = currentAttribute.clone();
        return currentAttribute;
    }

    public InputDevice inputDevice() {
        return inputDevice;
    }

    public OutputDeviceImpl inputDevice(InputDevice inputDevice) {
        this.inputDevice = inputDevice;

        return this;
    }


    @Override
    public OutputDevice remoteDeviceReady() {
        return this;
    }

    @Override
    public OutputDevice remoteRequestsStatusReport() {
        inputDevice.sendStatusReport();
        return this;
    }

    @Override
    public OutputDevice remoteDeviceBusy(boolean mustRequestAgain) {
        return this;
    }

    @Override
    public OutputDevice remoteDeviceMalfunction(boolean mustRequestAgain) {
        return this;
    }

    @Override
    public OutputDevice remoteRequestsPosition() {
        inputDevice.reportPosition(model.currentLine(), model.currentColumn());
        return this;
    }
}
