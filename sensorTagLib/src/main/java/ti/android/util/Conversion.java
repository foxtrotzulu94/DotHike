package ti.android.util;

import java.util.Formatter;

/* This class encapsulates utility functions */
public class Conversion {

  public static byte loUint16(short v) {
    return (byte) (v & 0xFF);
  }

  public static byte hiUint16(short v) {
    return (byte) (v >> 8);
  }

  public static short buildUint16(byte hi, byte lo) {
    return (short) ((hi << 8) + (lo & 0xff));
  }

  public static String BytetohexString(byte[] b, int len) {
    StringBuilder sb = new StringBuilder(b.length * (2 + 1));
    Formatter formatter = new Formatter(sb);

    for (int i = 0; i < len; i++) {
      if (i < len - 1)
        formatter.format("%02X:", b[i]);
      else
        formatter.format("%02X", b[i]);

    }
    formatter.close();

    return sb.toString();
  }

  static String BytetohexString(byte[] b, boolean reverse) {
    StringBuilder sb = new StringBuilder(b.length * (2 + 1));
    Formatter formatter = new Formatter(sb);

    if (!reverse) {
      for (int i = 0; i < b.length; i++) {
        if (i < b.length - 1)
          formatter.format("%02X:", b[i]);
        else
          formatter.format("%02X", b[i]);

      }
    } else {
      for (int i = (b.length - 1); i >= 0; i--) {
        if (i > 0)
          formatter.format("%02X:", b[i]);
        else
          formatter.format("%02X", b[i]);

      }
    }
    formatter.close();

    return sb.toString();
  }

  // Convert hex String to Byte
  public static int hexStringtoByte(String sb, byte[] results) {

    int i = 0;
    boolean j = false;

    if (sb != null) {
      for (int k = 0; k < sb.length(); k++) {
        if (((sb.charAt(k)) >= '0' && (sb.charAt(k) <= '9')) || ((sb.charAt(k)) >= 'a' && (sb.charAt(k) <= 'f'))
            || ((sb.charAt(k)) >= 'A' && (sb.charAt(k) <= 'F'))) {
          if (j) {
            results[i] += (byte) (Character.digit(sb.charAt(k), 16));
            i++;
          } else {
            results[i] = (byte) (Character.digit(sb.charAt(k), 16) << 4);
          }
          j = !j;
        }
      }
    }
    return i;
  }

  public static boolean isAsciiPrintable(String str) {
    if (str == null) {
      return false;
    }
    int sz = str.length();
    for (int i = 0; i < sz; i++) {
      if (isAsciiPrintable(str.charAt(i)) == false) {
        return false;
      }
    }
    return true;
  }

  private static boolean isAsciiPrintable(char ch) {
    return ch >= 32 && ch < 127;
  }

}