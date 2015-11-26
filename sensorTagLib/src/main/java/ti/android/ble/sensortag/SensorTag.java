package ti.android.ble.sensortag;

import static java.util.UUID.fromString;

import java.util.UUID;

public class SensorTag {

  public final static UUID 
      UUID_IRT_SERV = fromString("f000aa00-0451-4000-b000-000000000000"),
      UUID_IRT_DATA = fromString("f000aa01-0451-4000-b000-000000000000"),
      UUID_IRT_CONF = fromString("f000aa02-0451-4000-b000-000000000000"), // 0: disable, 1: enable
      UUID_IRT_PERI = fromString("f000aa03-0451-4000-b000-000000000000"), // Period in tens of milliseconds

      UUID_ACC_SERV = fromString("f000aa10-0451-4000-b000-000000000000"),
      UUID_ACC_DATA = fromString("f000aa11-0451-4000-b000-000000000000"),
      UUID_ACC_CONF = fromString("f000aa12-0451-4000-b000-000000000000"), // 0: disable, 1: enable
      UUID_ACC_PERI = fromString("f000aa13-0451-4000-b000-000000000000"), // Period in tens of milliseconds

      UUID_HUM_SERV = fromString("f000aa20-0451-4000-b000-000000000000"),
      UUID_HUM_DATA = fromString("f000aa21-0451-4000-b000-000000000000"),
      UUID_HUM_CONF = fromString("f000aa22-0451-4000-b000-000000000000"), // 0: disable, 1: enable
      UUID_HUM_PERI = fromString("f000aa23-0451-4000-b000-000000000000"), // Period in tens of milliseconds

      UUID_MAG_SERV = fromString("f000aa30-0451-4000-b000-000000000000"),
      UUID_MAG_DATA = fromString("f000aa31-0451-4000-b000-000000000000"),
      UUID_MAG_CONF = fromString("f000aa32-0451-4000-b000-000000000000"), // 0: disable, 1: enable
      UUID_MAG_PERI = fromString("f000aa33-0451-4000-b000-000000000000"), // Period in tens of milliseconds

      UUID_BAR_SERV = fromString("f000aa40-0451-4000-b000-000000000000"), 
      UUID_BAR_DATA = fromString("f000aa41-0451-4000-b000-000000000000"),
      UUID_BAR_CONF = fromString("f000aa42-0451-4000-b000-000000000000"), // 0: disable, 1: enable
      UUID_BAR_CALI = fromString("f000aa43-0451-4000-b000-000000000000"), // Calibration characteristic
      UUID_BAR_PERI = fromString("f000aa44-0451-4000-b000-000000000000"), // Period in tens of milliseconds

      UUID_GYR_SERV = fromString("f000aa50-0451-4000-b000-000000000000"), 
      UUID_GYR_DATA = fromString("f000aa51-0451-4000-b000-000000000000"),
      UUID_GYR_CONF = fromString("f000aa52-0451-4000-b000-000000000000"), // 0: disable, bit 0: enable x, bit 1: enable y, bit 2: enable z
      UUID_GYR_PERI = fromString("f000aa53-0451-4000-b000-000000000000"), // Period in tens of milliseconds

      UUID_KEY_SERV = fromString("0000ffe0-0000-1000-8000-00805f9b34fb"), 
      UUID_KEY_DATA = fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
}
