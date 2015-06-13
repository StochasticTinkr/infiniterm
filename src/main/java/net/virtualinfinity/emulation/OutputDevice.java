package net.virtualinfinity.emulation;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public interface OutputDevice {
    OutputDevice received(CharSequence chars);
    OutputDevice intensity(Intensity intensity);
    OutputDevice italicized(boolean italicized);
    OutputDevice underline(Underline underline);
    OutputDevice clearRenditionAttributes();
    OutputDevice blinking(Blinking blinking);
    OutputDevice inverseDisplay(boolean inverse);
    OutputDevice concealed(boolean concealed);
    OutputDevice useFont(int fontIndex);
    OutputDevice strikeThrough(boolean strikeThrough);
    OutputDevice setForeground(Color color);
    OutputDevice setBackground(Color color);
    OutputDevice resetForeground();
    OutputDevice resetBackground();
    OutputDevice framed(boolean framed);
    OutputDevice encircled(boolean encircled);
    OutputDevice overlined(boolean overlined);
    OutputDevice ideogramUnderline(Underline underline);
    OutputDevice ideogramOverline(Underline underline);
    OutputDevice ideogramStressMarking(boolean stressMarking);
    OutputDevice cursorLeft(int count);
    OutputDevice cursorDown(int count);
    OutputDevice cursorRight(int count);
    OutputDevice cursorUp(int count);
    OutputDevice bell();
    OutputDevice backspace();
    OutputDevice acknowledge(boolean positive);
    OutputDevice nul();
    OutputDevice startOfHeading();
    OutputDevice cancel();
    OutputDevice carriageReturn();
    OutputDevice enableManualInput(boolean enabled);
    OutputDevice formFeed();
    OutputDevice horizontalTab();
    OutputDevice lineFeed();
    OutputDevice startOfText();
    OutputDevice verticalTab();
    OutputDevice breakPermittedHere(boolean isPermitted);
    OutputDevice cancelCharacter();
    OutputDevice markHorizontalTab();
    OutputDevice cursorPreviousLine(int lines);
    OutputDevice nextLine();
    OutputDevice cursorNextLine(int lines);
    OutputDevice partialLineForward();
    OutputDevice partialLineBackward();
    OutputDevice reverseLineFeed();
    OutputDevice markVerticalTab();
    OutputDevice interrupt();
    OutputDevice reset();
    OutputDevice cursorCharacterPosition(int characterPosition);
    OutputDevice line(int line);
    OutputDevice delete(int count);
    OutputDevice deleteLine(int count);
    OutputDevice setDimensions(int lineDimension, int positionDimension);
    OutputDevice eraseInAreaToCursor(AreaType type);
    OutputDevice eraseInAreaAfterCursor(AreaType type);
    OutputDevice eraseArea(AreaType type);
    OutputDevice eraseCharacter(int count);
    OutputDevice setFont(int fontIndex, int fontId);
    OutputDevice insertCharacter(int count);
    OutputDevice insertLine(int count);
    OutputDevice repeatPreviousCharacter(int count);
    OutputDevice scrollDown(int count);
    OutputDevice scrollLeft(int count);
    OutputDevice scrollRight(int count);
    OutputDevice scrollUp(int count);

    default OutputDevice cursorPosition(int line, int characterPosition) {
        return line(line).cursorCharacterPosition(characterPosition);
    }
    OutputDevice remoteDeviceReady();
    OutputDevice remoteRequestsStatusReport();
    OutputDevice remoteDeviceBusy(boolean mustRequestAgain);
    OutputDevice remoteDeviceMalfunction(boolean mustRequestAgain);
    OutputDevice remoteRequestsPosition();
}
