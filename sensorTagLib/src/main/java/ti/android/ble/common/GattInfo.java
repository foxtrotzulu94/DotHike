package ti.android.ble.common;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.res.XmlResourceParser;

public class GattInfo {
  // Bluetooth SIG identifiers
  public static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
  private static final String uuidBtSigBase = "0000****-0000-1000-8000-00805f9b34fb";
  private static final String uuidTiBase = "f000****-0451-4000-b000-000000000000";

  public static final UUID OAD_SERVICE_UUID = UUID.fromString("f000ffc0-0451-4000-b000-000000000000");
  public static final UUID CC_SERVICE_UUID = UUID.fromString("f000ccc0-0451-4000-b000-000000000000");

  private static Map<String, String> mNameMap = new HashMap<String, String>();
  private static Map<String, String> mDescrMap = new HashMap<String, String>();

  public GattInfo(XmlResourceParser xpp) {
    // XML data base
    try {
      readUuidData(xpp);
    } catch (XmlPullParserException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static String uuidToName(UUID uuid) {
    String str = toShortUuidStr(uuid);
    return uuidToName(str.toUpperCase());
  }

  public static String getDescription(UUID uuid) {
    String str = toShortUuidStr(uuid);
    return mDescrMap.get(str.toUpperCase());
  }

  static public boolean isTiUuid(UUID u) {
    String us = u.toString();
    String r = toShortUuidStr(u);
    us = us.replace(r, "****");
    return us.equals(uuidTiBase);
  }

  static public boolean isBtSigUuid(UUID u) {
    String us = u.toString();
    String r = toShortUuidStr(u);
    us = us.replace(r, "****");
    return us.equals(uuidBtSigBase);
  }

  static public String uuidToString(UUID u) {
    String uuidStr;
    if (isBtSigUuid(u))
      uuidStr = GattInfo.toShortUuidStr(u);
    else
      uuidStr = u.toString();
    return uuidStr.toUpperCase();
  }

  static private String toShortUuidStr(UUID u) {
    return u.toString().substring(4, 8);
  }

  private static String uuidToName(String uuidStr16) {
    return mNameMap.get(uuidStr16);
  }

  //
  // XML loader
  //
  private void readUuidData(XmlResourceParser xpp) throws XmlPullParserException, IOException {
    xpp.next();
    String tagName = null;
    String uuid = null;
    String descr = null;
    int eventType = xpp.getEventType();

    while (eventType != XmlPullParser.END_DOCUMENT) {
      if (eventType == XmlPullParser.START_DOCUMENT) {
        // do nothing
      } else if (eventType == XmlPullParser.START_TAG) {
        tagName = xpp.getName();
        uuid = xpp.getAttributeValue(null, "uuid");
        descr = xpp.getAttributeValue(null, "descr");
      } else if (eventType == XmlPullParser.END_TAG) {
        // do nothing
      } else if (eventType == XmlPullParser.TEXT) {
        if (tagName.equalsIgnoreCase("item")) {
          if (!uuid.isEmpty()) {
            uuid = uuid.replace("0x", "");
            mNameMap.put(uuid, xpp.getText());
            mDescrMap.put(uuid, descr);
          }
        }
      }
      eventType = xpp.next();
    }
  }
}
