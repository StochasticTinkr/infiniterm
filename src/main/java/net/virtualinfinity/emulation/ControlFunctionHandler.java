package net.virtualinfinity.emulation;

import org.apache.log4j.Logger;

/**
 * @author <a href='mailto:Daniel@coloraura.com'>Daniel Pitts</a>
 */
public class ControlFunctionHandler {
    private static final Logger logger = Logger.getLogger(ControlFunctionHandler.class);
    //    public  static final int CBT  = 0x005A;
    public  static final int CHA  = 0x0047;
//    public  static final int CHT  = 0x0049;
    public  static final int CNL  = 0x0045;
    public  static final int CPL  = 0x0046;
    public  static final int CPR  = 0x0052;
    public  static final int CTC  = 0x0057;
    public  static final int CUB  = 0x0044;
    public  static final int CUD  = 0x0042;
    public  static final int CUF  = 0x0043;
    public  static final int CUP  = 0x0048;
    public  static final int CUU  = 0x0041;
//    public  static final int CVT  = 0x0059;
//    public  static final int DA   = 0x0063;
//    public  static final int DAQ  = 0x006F;
    public  static final int DCH  = 0x0050;
    public  static final int DL   = 0x004D;
    public  static final int DSR  = 0x006E;
    public  static final int DTA  = 0x2054;
    public  static final int EA   = 0x004F;
    public  static final int ECH  = 0x0058;
    public  static final int ED   = 0x004A;
    public  static final int EF   = 0x004E;
    public  static final int EL   = 0x004B;
//    public  static final int FNK  = 0x2057;
    public  static final int FNT  = 0x2044;
//    public  static final int GCC  = 0x205F;
//    public  static final int GSM  = 0x2042;
//    public  static final int GSS  = 0x2043;
//    public  static final int HPA  = 0x0060;
//    public  static final int HPB  = 0x006A;
//    public  static final int HPR  = 0x0061;
//    public  static final int HVP  = 0x0066;
    public  static final int ICH  = 0x0040;
//    public  static final int IDCS = 0x204F;
//    public  static final int IGS  = 0x204D;
    public  static final int IL   = 0x004C;
//    public  static final int JFY  = 0x2046;
//    public  static final int MC   = 0x0069;
//    public  static final int NP   = 0x0055;
//    public  static final int PEC  = 0x205A;
//    public  static final int PFS  = 0x204A;
//    public  static final int PP   = 0x0056;
//    public  static final int PPA  = 0x2050;
//    public  static final int PPB  = 0x2052;
//    public  static final int PPR  = 0x2051;
//    public  static final int PTX  = 0x005C;
//    public  static final int QUAD = 0x2048;
    public  static final int REP  = 0x0062;
//    public  static final int RM   = 0x006C;
//    public  static final int SACS = 0x205C;
//    public  static final int SAPV = 0x205D;
//    public  static final int SCO  = 0x2065;
//    public  static final int SCP  = 0x206B;
//    public  static final int SCS  = 0x2067;
    public  static final int SD   = 0x0054;
//    public  static final int SDS  = 0x005D;
//    public  static final int SEE  = 0x0051;
//    public  static final int SEF  = 0x2059;
    public  static final int SGR  = 0x006D;
//    public  static final int SHS  = 0x204B;
//    public  static final int SIMD = 0x005E;
    public  static final int SL   = 0x2040;
//    public  static final int SLH  = 0x2055;
//    public  static final int SLL  = 0x2056;
//    public  static final int SLS  = 0x2068;
//    public  static final int SM   = 0x0068;
//    public  static final int SPD  = 0x2053;
//    public  static final int SPH  = 0x2069;
//    public  static final int SPI  = 0x2047;
//    public  static final int SPL  = 0x206A;
//    public  static final int SPQR = 0x2058;
    public  static final int SR   = 0x2041;
//    public  static final int SRCS = 0x2066;
//    public  static final int SRS  = 0x005B;
//    public  static final int SSU  = 0x2049;
//    public  static final int SSW  = 0x205B;
//    public  static final int STAB = 0x205E;
    public  static final int SU   = 0x0053;
//    public  static final int SVS  = 0x204C;
//    public  static final int TAC  = 0x2062;
//    public  static final int TALE = 0x2061;
//    public  static final int TATE = 0x2060;
//    public  static final int TBC  = 0x0067;
//    public  static final int TCC  = 0x2063;
//    public  static final int TSR  = 0x2064;
//    public  static final int TSS  = 0x2045;
//    public  static final int VPA  = 0x0064;
//    public  static final int VPB  = 0x006B;
//    public  static final int VPR  = 0x0065;

    private static final GraphicRenditionSelector grs = new GraphicRenditionSelector();
    public  static final int ERASE_CURSOR_TO_BOTTOM = 0;
    public  static final int ERASE_TOP_TO_CURSOR = 1;
    public  static final int ERASE_ENTIRE_AREA = 2;
    public  static final int CTC_SET_AT_CURRENT_POSITION = 0;
    public  static final int CTC_SET_AT_CURRENT_LINE = 1;
    public  static final int CTC_CLEAR_AT_CURRENT_POSITION = 2;
    public  static final int CTC_CLEAR_AT_CURRENT_LINE = 3;
    public  static final int CTC_CLEAR_ALL_ON_LINE = 4;
    public  static final int CTC_CLEAR_ALL_POSITION_STOPS = 5;
    public  static final int CTC_CLEAR_ALL_LINE_STOP = 6;
    public  static final int DSR_READY = 0;
    public  static final int DSR_BUSY_REQUEST_LATER = 1;
    public  static final int DSR_BUSY_WILL_SEND_LATER = 2;
    public  static final int DSR_MALFUNCTION_REQUEST_LATER = 3;
    public  static final int DSR_MALFUNCTION_WILL_SEND_LATER = 4;
    public  static final int DSR_REQUESTED = 5;
    public  static final int DSR_POSITION_REQUESTED = 6;

    public OutputDevice dispatch(byte[] function, byte[] parameters, OutputDevice device) {
        if (function.length > 4) {
            // I dunno what to do.
            return device;
        }
        int code = 0;
        for (final byte b: function) {
            code = code * 0x100 + ((int)b)&0xFF;
        }
        switch (code) {
//            case CBT:   return device.cursorBackTab();
            case CHA:   return device.cursorCharacterPosition(singleInt(parameters, 1));
//            case CHT:   return device.cursorNextTab();
            case CNL:   return device.cursorNextLine(countFrom(parameters));
            case CPL:   return device.cursorPreviousLine(countFrom(parameters));
//            case CPR:   return device.cursorPositionReport();
            case CTC:   return cursorTabulationControl(device, singleInt(parameters, 0));
            case CUB:   return device.cursorLeft(countFrom(parameters));
            case CUD:   return device.cursorDown(countFrom(parameters));
            case CUF:   return device.cursorRight(countFrom(parameters));
            case CUP:   return cursorPosition(device, intParams(parameters));
            case CUU:   return device.cursorUp(countFrom(parameters));
//            case CVT:   return device.cursorNextLineTab();
//            case DA:    return device;
//            case DAQ:   return defineArea(device,  singleInt(parameters, 0));
            case DCH:   return device.delete(countFrom(parameters));
            case DL:    return device.deleteLine(countFrom(parameters));
            case DSR:   return deviceStatusReport(device, singleInt(parameters, 0));
            case DTA:   return setDimension(device, intParams(parameters));
            case EA:    return erase(parameters, device, AreaType.AREA);
            case ECH:   return device.eraseCharacter(countFrom(parameters));
            case ED:    return erase(parameters, device, AreaType.PAGE);
            case EF:    return erase(parameters, device, AreaType.FIELD);
            case EL:    return erase(parameters, device, AreaType.LINE);
//            case FNK:   return device;
            case FNT:   return selectFont(device, intParams(parameters));
//            case GCC:   return device;
//            case GSM:   return device; // TODO: Maybe?
//            case GSS:   return device; // TODO: Maybe?
//            case HPA:   return device.cursorCharacterPosition(singleInt(parameters, 1));
//            case HPB:   return device;
//            case HPR:   return device;
//            case HVP:   return device;
            case ICH:   return device.insertCharacter(countFrom(parameters));
//            case IDCS:  return device;
//            case IGS:   return device;
            case IL:    return device.insertLine(countFrom(parameters));
//            case JFY:   return device; // TODO: Maybe?
//            case MC:    return device;
//            case NP:    return device;
//            case PEC:   return device;
//            case PFS:   return device;
//            case PP:    return device;
//            case PPA:   return device;
//            case PPB:   return device;
//            case PPR:   return device;
//            case PTX:   return device;
//            case QUAD:  return device;
            case REP:   return device.repeatPreviousCharacter(countFrom(parameters));
//            case RM:    return device;
//            case SACS:  return device;
//            case SAPV:  return device;
//            case SCO:   return device;
//            case SCP:   return device;
//            case SCS:   return device;
            case SD:    return device.scrollDown(countFrom(parameters));
//            case SDS:   return device;
//            case SEE:   return device;
//            case SEF:   return device;
            case SGR:   return setGraphicsAttributes(device, intParams(parameters));
//            case SHS:   return device;
//            case SIMD:  return device;
            case SL:    return device.scrollLeft(countFrom(parameters));
//            case SLH:   return device;
//            case SLL:   return device;
//            case SLS:   return device;
//            case SM:    return device; // TODO: Maybe?
//            case SPD:   return device;
//            case SPH:   return device;
//            case SPI:   return device;
//            case SPL:   return device;
//            case SPQR:  return device;
            case SR:    return device.scrollRight(countFrom(parameters));
//            case SRCS:  return device;
//            case SRS:   return device;
//            case SSU:   return device;
//            case SSW:   return device;
//            case STAB:  return device;
            case SU:    return device.scrollUp(countFrom(parameters));
//            case SVS:   return device;
//            case TAC:   return device;
//            case TALE:  return device;
//            case TATE:  return device;
//            case TBC:   return device;
//            case TCC:   return device;
//            case TSR:   return device;
//            case TSS:   return device;
//            case VPA:   return device;
//            case VPB:   return device;
//            case VPR:   return device;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Unhandled control function: " + Integer.toHexString(code));
        }

        return device;
    }

    private OutputDevice deviceStatusReport(OutputDevice device, int param) {
        switch (param) {
            case DSR_READY: return device.remoteDeviceReady();
            case DSR_BUSY_REQUEST_LATER: return device.remoteDeviceBusy(true);
            case DSR_BUSY_WILL_SEND_LATER: return device.remoteDeviceBusy(false);
            case DSR_MALFUNCTION_REQUEST_LATER: return device.remoteDeviceMalfunction(true);
            case DSR_MALFUNCTION_WILL_SEND_LATER: return device.remoteDeviceMalfunction(false);
            case DSR_REQUESTED: return device.remoteRequestsStatusReport();
            case DSR_POSITION_REQUESTED: return device.remoteRequestsPosition();
        }
        return device;
    }

    public int countFrom(byte[] parameters) {
        return singleInt(parameters, 1);
    }

    private OutputDevice selectFont(OutputDevice device, int[] params) {
        final int fontIndex = intAt(params, 0, 0);
        final int fontId = intAt(params, 0, 1);
        return device.setFont(fontIndex, fontId);
    }

    public OutputDevice erase(byte[] parameters, OutputDevice device, AreaType area) {
        switch (singleInt(parameters, ERASE_TOP_TO_CURSOR)) {
            case ERASE_TOP_TO_CURSOR:    return device.eraseInAreaToCursor(area);
            case ERASE_CURSOR_TO_BOTTOM: return device.eraseInAreaAfterCursor(area);
            case ERASE_ENTIRE_AREA:      return device.eraseArea(area);
            default: return device;
        }
    }

    private OutputDevice setDimension(OutputDevice device, int[] parameters) {
        if (parameters.length < 2) {
            return device;
        }
        return device.setDimensions(parameters[0], parameters[1]);
    }

    private OutputDevice cursorTabulationControl(OutputDevice device, int parameter) {
        switch (parameter) {
            case CTC_SET_AT_CURRENT_POSITION:   return device;
            case CTC_SET_AT_CURRENT_LINE:       return device;
            case CTC_CLEAR_AT_CURRENT_POSITION: return device;
            case CTC_CLEAR_AT_CURRENT_LINE:     return device;
            case CTC_CLEAR_ALL_ON_LINE:         return device;
            case CTC_CLEAR_ALL_POSITION_STOPS:  return device;
            case CTC_CLEAR_ALL_LINE_STOP:       return device;
        }
        return device;
    }

    public OutputDevice setGraphicsAttributes(OutputDevice device, int[] intParams) {
        for (final int value: intParams) {
            grs.dispatch(value, device);
        }
        return device;
    }

    public OutputDevice cursorPosition(OutputDevice device, int[] intParams) {
        return device.cursorPosition(intAt(intParams, 0, 1), intAt(intParams, 1, 1));
    }

    private static int intAt(int[] parameters, int n, int def) {
          return parameters.length >= n+1 ? parameters[n]:  def;
      }


      private static int singleInt(byte[] parameters, int def) {
          if (parameters.length == 0) {
              return def;
          }
          return intParams(parameters)[0];
      }

      private static int[] intParams(byte[] parameters) {
          if (parameters.length == 0) {
              return new int[0];
          }
          int numParams = 1;
          for (final byte b:  parameters) {
              if (b == ';') {
                  ++numParams;
              }
          }
          final int[] intParams = new int[numParams];
          int idx = 0;
          for (final byte b:  parameters) {
              if (b == ';') {
                  ++idx;
              } else {
                  if (b >= '0' && b <= '9') {
                      intParams[idx] = intParams[idx] * 10 + b - '0';
                  }
              }
          }
          return intParams;
      }

}
