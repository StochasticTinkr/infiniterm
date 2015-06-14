package net.virtualinfinity.emulation;

import java.io.ByteArrayOutputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public class Decoder {
    private static final State STATE_ESC = new Escape();
    private static final State NORMAL = new Normal();
    public static final byte ESC = (byte)0x1B;
    public static final byte ESCAPED_CSI = (byte)0x5B;

    private final OutputDevice device;
    private CharsetDecoder characterDecoder = Charset.defaultCharset().newDecoder();
    private static final ControlFunctionHandler cfh = new ControlFunctionHandler();
    private State state = NORMAL;
    private final CharBuffer charBuffer = CharBuffer.allocate(10000);
    private final ByteBuffer inputBuffer = ByteBuffer.allocate(10000);

    public Decoder(OutputDevice device) {
        this.device = device;
    }

    public synchronized void received(ByteBuffer data) {
        while (data.hasRemaining()) {
            state = state.accept(data.get(), this);
        }
        flush();
    }

    private Decoder flush() {
        return flush(false);
    }

    public synchronized Decoder flush(boolean endOfInput) {
        inputBuffer.flip();
        CoderResult result;
        do {
            result = characterDecoder.decode(inputBuffer, charBuffer, endOfInput);
            if (result.isMalformed() && inputBuffer.hasRemaining()) {
                inputBuffer.get();
            }
        } while (result.isMalformed() && inputBuffer.hasRemaining());
        if (!endOfInput) {
            inputBuffer.compact();
        } else {
            inputBuffer.clear();
        }
        charBuffer.flip();
        if (charBuffer.hasRemaining()) {
            device.received(charBuffer);
        }
        charBuffer.clear();

        return this;
    }

    private void normalByte(byte b) {
        if (!inputBuffer.hasRemaining()) {
            flush();
        }
        try {
            inputBuffer.put(b);
        } catch (BufferOverflowException bfo) {
            bfo.printStackTrace();
        }
    }


    private OutputDevice dispatchControlSequence(byte[] function, byte[] parameters) {
        flush();
        return cfh.dispatch(function, parameters, device);
    }

    public OutputDevice device() {
        return device;
    }

    interface State {
        State accept(byte b, Decoder decoder);
    }

    private static class ControlSequence implements State {
        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        @Override
        public State accept(byte b, Decoder decoder) {
            if (b >= 0x30 && b <= 0x3F) {
                buffer.write(b);
            } else {
                final byte[] parameters = buffer.toByteArray();
                return new State() {
                    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    @Override
                    public State accept(byte b, Decoder decoder) {
                        if (b >= 0x20 && b <= 0x2F) {
                            buffer.write(b);
                            return this;
                        }
                        if (b >= 0x40 && b <= 0x7E) {
                            buffer.write(b);
                            decoder.dispatchControlSequence(buffer.toByteArray(), parameters);
                            return NORMAL;
                        }
                        // TODO: Report error.
                        return NORMAL;

                    }
                }.accept(b, decoder);
            }
            return this;
        }
    }

    private static class Escape implements State {
        //        private static final byte CMD  = (byte)0x64;
//        private static final byte DMI  = (byte)0x60;
//        private static final byte EMI  = (byte)0x62;
        private static final byte INT  = (byte)0x61;
        //        private static final byte LS1R = (byte)0x7E;
//        private static final byte LS2  = (byte)0x6E;
//        private static final byte LS2R = (byte)0x7D;
//        private static final byte LS3  = (byte)0x6F;
//        private static final byte LS3R = (byte)0x7C;
        private static final byte RIS  = (byte)0x63;
        private static final byte APC  = (byte)0x5F;
        private static final byte BPH  = (byte)0x42;
        private static final byte CCH  = (byte)0x54;
        private static final byte CSI  = ESCAPED_CSI;
        private static final byte DCS  = (byte)0x50;
        private static final byte EPA  = (byte)0x57;
        private static final byte ESA  = (byte)0x47;
        private static final byte HTJ  = (byte)0x49;
        private static final byte HTS  = (byte)0x48;
        private static final byte MW   = (byte)0x55;
        private static final byte NBH  = (byte)0x43;
        private static final byte NEL  = (byte)0x45;
        private static final byte OSC  = (byte)0x5D;
        private static final byte PLD  = (byte)0x4B;
        private static final byte PLU  = (byte)0x4C;
        private static final byte PM   = (byte)0x5E;
        private static final byte PU1  = (byte)0x51;
        private static final byte PU2  = (byte)0x52;
        private static final byte RI   = (byte)0x4D;
        private static final byte SCI  = (byte)0x5A;
        private static final byte SOS  = (byte)0x58;
        private static final byte SPA  = (byte)0x56;
        private static final byte SSA  = (byte)0x46;
        private static final byte SS2  = (byte)0x4E;
        private static final byte SS3  = (byte)0x4F;
        private static final byte ST   = (byte)0x5C;
        private static final byte STS  = (byte)0x53;
        private static final byte VTS  = (byte)0x4A;

        @Override
        public State accept(byte b, Decoder decoder) {
            switch (b) {
                case APC:  return NORMAL.accept(Normal.APC, decoder);
                case BPH:  return NORMAL.accept(Normal.BPH, decoder);
                case CCH:  return NORMAL.accept(Normal.CCH, decoder);
                case CSI:  return NORMAL.accept(Normal.CSI, decoder);
                case DCS:  return NORMAL.accept(Normal.DCS, decoder);
                case EPA:  return NORMAL.accept(Normal.EPA, decoder);
                case ESA:  return NORMAL.accept(Normal.ESA, decoder);
                case HTJ:  return NORMAL.accept(Normal.HTJ, decoder);
                case HTS:  return NORMAL.accept(Normal.HTS, decoder);
                case MW:   return NORMAL.accept(Normal.MW,  decoder);
                case NBH:  return NORMAL.accept(Normal.NBH, decoder);
                case NEL:  return NORMAL.accept(Normal.NEL, decoder);
                case OSC:  return NORMAL.accept(Normal.OSC, decoder);
                case PLD:  return NORMAL.accept(Normal.PLD, decoder);
                case PLU:  return NORMAL.accept(Normal.PLU, decoder);
                case PM:   return NORMAL.accept(Normal.PM,  decoder);
                case PU1:  return NORMAL.accept(Normal.PU1, decoder);
                case PU2:  return NORMAL.accept(Normal.PU2, decoder);
                case RI:   return NORMAL.accept(Normal.RI,  decoder);
                case SCI:  return NORMAL.accept(Normal.SCI, decoder);
                case SOS:  return NORMAL.accept(Normal.SOS, decoder);
                case SPA:  return NORMAL.accept(Normal.SPA, decoder);
                case SSA:  return NORMAL.accept(Normal.SSA, decoder);
                case SS2:  return NORMAL.accept(Normal.SS2, decoder);
                case SS3:  return NORMAL.accept(Normal.SS3, decoder);
                case ST:   return NORMAL.accept(Normal.ST,  decoder);
                case STS:  return NORMAL.accept(Normal.STS, decoder);
                case VTS:  return NORMAL.accept(Normal.VTS, decoder);
//                case CMD:  return NORMAL;
//                case DMI: decoder.device.setInputEnabled(false); return NORMAL;
//                case EMI: decoder.device.setInputEnabled(true); return NORMAL;
                case INT:  decoder.device.interrupt(); return NORMAL;
//                case LS1R: return NORMAL;
//                case LS2:  return NORMAL;
//                case LS2R: return NORMAL;
//                case LS3:  return NORMAL;
//                case LS3R: return NORMAL;
                case RIS:  decoder.device.reset(); return NORMAL;
            }
            decoder.normalByte(b);

            return NORMAL;
        }
    }

    public synchronized Decoder charset(Charset charset) {
        flush(true);
        characterDecoder = charset.newDecoder();
        return this;
    }

    public synchronized Charset charset() {
        return characterDecoder.charset();
    }


    private static class Normal implements State {
        private static final byte ACK = (byte)0x06;
        private static final byte BEL = (byte)0x07;
        private static final byte BS  = (byte)0x08;
        private static final byte CAN = (byte)0x18;
        private static final byte CR  = (byte)0x0D;
        //        private static final byte DC1 = (byte)0x11;
//        private static final byte DC2 = (byte)0x12;
//        private static final byte DC3 = (byte)0x13;
//        private static final byte DC4 = (byte)0x14;
//        private static final byte DLE = (byte)0x10;
        private static final byte EM  = (byte)0x19;
        //        private static final byte ENQ = (byte)0x05;
//        private static final byte EOT = (byte)0x04;
        //        private static final byte ETB = (byte)0x17;
//        private static final byte ETX = (byte)0x03;
        private static final byte FF  = (byte)0x0C;
        private static final byte HT  = (byte)0x09;
        //        private static final byte IS1 = (byte)0x1F;
//        private static final byte IS2 = (byte)0x1E;
//        private static final byte IS3 = (byte)0x1D;
//        private static final byte IS4 = (byte)0x1C;
        private static final byte LF  = (byte)0x0A;
        //        private static final byte LS0 = (byte)0x0F;
//        private static final byte LS1 = (byte)0x0E;
        private static final byte NAK = (byte)0x15;
        private static final byte NUL = (byte)0x00;
        //        private static final byte SI  = (byte)0x0F;
//        private static final byte SO  = (byte)0x0E;
        private static final byte SOH = (byte)0x01;
        private static final byte STX = (byte)0x02;
        //        private static final byte SUB = (byte)0x1A;
        private static final byte SYN = (byte)0x16;
        private static final byte VT  = (byte)0x0B;
        private static final byte APC = (byte)0x9F;
        private static final byte BPH = (byte)0x82;
        private static final byte CCH = (byte)0x94;
        private static final byte CSI = (byte)0x9B;
        private static final byte DCS = (byte)0x90;
        private static final byte EPA = (byte)0x97;
        private static final byte ESA = (byte)0x87;
        private static final byte HTJ = (byte)0x89;
        private static final byte HTS = (byte)0x88;
        private static final byte MW  = (byte)0x95;
        private static final byte NBH = (byte)0x83;
        private static final byte NEL = (byte)0x85;
        private static final byte OSC = (byte)0x9D;
        private static final byte PLD = (byte)0x8B;
        private static final byte PLU = (byte)0x8C;
        private static final byte PM  = (byte)0x9E;
        private static final byte PU1 = (byte)0x91;
        private static final byte PU2 = (byte)0x92;
        private static final byte RI  = (byte)0x8D;
        private static final byte SCI = (byte)0x9A;
        private static final byte SOS = (byte)0x98;
        private static final byte SPA = (byte)0x96;
        private static final byte SSA = (byte)0x86;
        private static final byte SS2 = (byte)0x8E;
        private static final byte SS3 = (byte)0x8F;
        private static final byte ST  = (byte)0x9C;
        private static final byte STS = (byte)0x93;
        private static final byte VTS = (byte)0x8A;


        @Override
        public State accept(byte b, Decoder decoder) {
            switch (b) {
                case BEL: decoder.flush().device.bell(); return this;
                case BS: decoder.flush().device.backspace(); return this;
                case ACK: decoder.flush().device.acknowledge(true); return this;
                case ESC: return STATE_ESC;
                case CSI: return new ControlSequence();
                case CAN:  decoder.flush().device.cancel(); return this;
                case CR :  decoder.flush().device.carriageReturn(); return this;
//                case DC1:  decoder.flush().device.deviceControl1(); return this;
//                case DC2:  decoder.flush().device.deviceControl2(); return this;
//                case DC3:  decoder.flush().device.deviceControl3(); return this;
//                case DC4:  decoder.flush().device.deviceControl4(); return this;
//                case DLE:  decoder.flush().device.dataLinkEscape(); return this;
                case EM :  decoder.flush().device.enableManualInput(false); return this;
//                case ENQ:  decoder.flush().device.enquiry(); return this;
//                case EOT:  decoder.flush().device.endOfTransmission(); return this;
//                case ETB:  decoder.flush().device.endOfTransmissionBlock(); return this;
//                case ETX:  decoder.flush().device.endOfText(); return this;
                case FF :  decoder.flush().device.formFeed(); return this;
                case HT :  decoder.flush().device.horizontalTab(); return this;
//                case IS1:  decoder.flush().device.unitSeparator(); return this;
//                case IS2:  decoder.flush().device.recordSeparator(); return this;
//                case IS3:  decoder.flush().device.groupSeparator(); return this;
//                case IS4:  decoder.flush().device.fileSeparator(); return this;
                case LF :  decoder.flush().device.lineFeed(); return this;
//                case LS0:  decoder.flush().device.; return this;
//                case LS1:  decoder.flush().device.; return this;
                case NAK:  decoder.flush().device.acknowledge(false); return this;
                case NUL:  decoder.flush().device.nul(); return this;
//                case SI :  decoder.flush().device.; return this;
//                case SO :  decoder.flush().device.; return this;
                case SOH:  decoder.flush().device.startOfHeading(); return this;
                case STX:  decoder.flush().device.startOfText(); return this;
//                case SUB:  decoder.flush().device.; return this;
                case SYN:  return this;
                case VT :  decoder.flush().device.verticalTab(); return this;
//                case APC:  decoder.flush().device.; return this;
                case BPH:  decoder.flush().device.breakPermittedHere(true); return this;
                case CCH:  decoder.flush().device.cancelCharacter(); return this;
//                case DCS:  decoder.flush().device.; return this;
//                case EPA:  decoder.flush().device.endGuardedArea(); return this;
//                case ESA:  decoder.flush().device.; return this;
//                case HTJ:  decoder.flush().device.; return this;
                case HTS:  decoder.flush().device.markHorizontalTab(); return this;
//                case MW :  decoder.flush().device.; return this;
                case NBH:  decoder.flush().device.breakPermittedHere(false); return this;
                case NEL:  decoder.flush().device.nextLine(); return this;
//                case OSC:  decoder.flush().device.; return this;
                case PLD:  decoder.flush().device.partialLineForward(); return this;
                case PLU:  decoder.flush().device.partialLineBackward(); return this;
//                case PM :  decoder.flush().device.; return this;
//                case PU1:  decoder.flush().device.; return this;
//                case PU2:  decoder.flush().device.; return this;
                case RI :  decoder.flush().device.reverseLineFeed(); return this;
//                case SCI:  decoder.flush().device.; return this;
//                case SOS:  decoder.flush().device.; return this;
//                case SPA:  decoder.flush().device.startGuardedArea(); return this;
//                case SSA:  decoder.flush().device.; return this;
//                case SS2:  decoder.flush().device.; return this;
//                case SS3:  decoder.flush().device.; return this;
//                case ST :  decoder.flush().device.; return this;
//                case STS:  decoder.flush().device.; return this;
                case VTS:  decoder.flush().device.markVerticalTab(); return this;
            }

            decoder.normalByte(b);

            return this;
        }
    }
}



