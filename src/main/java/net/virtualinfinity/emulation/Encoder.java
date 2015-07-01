package net.virtualinfinity.emulation;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public class Encoder {
    public static final Charset US_ASCII = Charset.forName("US-ASCII");
    private final Consumer<ByteBuffer> consumer;
    private CharsetEncoder characterEncoder = Charset.defaultCharset().newEncoder();
    private final ByteBuffer buffer = ByteBuffer.allocate(65536);

    public Encoder(Consumer<ByteBuffer> consumer) {
        this.consumer = consumer;
    }

    public void up() {
        flushCharacters();
        sendCSI(ControlFunctionHandler.CUU);
    }

    public void down() {
        flushCharacters();
        sendCSI(ControlFunctionHandler.CUD);
    }

    public void right() {
        flushCharacters();
        sendCSI(ControlFunctionHandler.CUF);
    }

    public void flushCharacters() {
        doEncodeOperation(encodeOperation(CharBuffer.wrap(""), true));
        doEncodeOperation(CharsetEncoder::flush);
        characterEncoder.reset();
    }

    public void left() {
        flushCharacters();
        sendCSI(ControlFunctionHandler.CUB);
    }

    public Encoder charset(Charset charset) {
        flush(true);
        characterEncoder = charset.newEncoder();
        return this;
    }

    public void flush(boolean endOfInput) {
        if (endOfInput) {
            flushCharacters();
        }
        buffer.flip();
        consumer.accept(buffer);
        buffer.compact();
    }

    public Charset charset() {
        return characterEncoder.charset();
    }

    private void sendCSI(int command) {
        writeByte(Decoder.ESC);
        writeByte(Decoder.ESCAPED_CSI);
        if (command >= 0x100) {
            writeByte((byte) (command / 0x100));
        }
        writeByte((byte)(command));
    }

    private void sendCSI(int command, int...parameters) {
        writeByte(Decoder.ESC);
        writeByte(Decoder.ESCAPED_CSI);
        for (int i = 0; i < parameters.length; ++i) {
            if (i != 0) {
                writeByte((byte)';');
            }
            for (final byte b : String.valueOf(parameters[i]).getBytes(US_ASCII)) {
                writeByte(b);
            }
        }
        if (command >= 0x100) {
            writeByte((byte) (command / 0x100));
        }
        writeByte((byte)(command));
    }


    private void writeByte(byte b) {
        if (!buffer.hasRemaining()) {
            flush(false);
        }
        buffer.put(b);
    }

    public void character(CharBuffer input) {
        doEncodeOperation(encodeOperation(input, false));
    }

    private void doEncodeOperation(BiFunction<CharsetEncoder, ByteBuffer, CoderResult> encodeOperation) {
        while (encodeOperation.apply(characterEncoder, buffer).isOverflow()) {
            flush(false);
        }
    }

    private BiFunction<CharsetEncoder, ByteBuffer, CoderResult> encodeOperation(CharBuffer input, boolean endOfInput) {
        return (encoder, buffer) -> encoder.encode(input, buffer, endOfInput);
    }


    public void statusReport() {
        sendCSI(ControlFunctionHandler.DSR, ControlFunctionHandler.DSR_READY);
    }

    public void reportPosition(int currentLine, int currentColumn) {
        sendCSI(ControlFunctionHandler.CPR, currentLine + 1, currentColumn + 1);
    }
}
