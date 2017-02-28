package com.giovastk.stk500.arduino;

/**
 * Intel Hex parser
 * For more info: https://en.wikipedia.org/wiki/Intel_HEX
 */
public class IntelHexParser
{
    private static final int DATA = 0;
    private static final int EOF = 1;
    private static final int EXTENDED_SEGMENT_ADDRESS = 2;
    private static final int START_SEGMENT_ADDRESS = 3;
    private static final int EXTENDED_LINEAR_ADDRESS = 4;
    private static final int START_LINEAR_ADDRESS = 5;

    public static ArduinoSketch parseHexFile(String hexData)
    {
        String[] splitData = hexData.split("\\r\\n|\\n|\\r");

        ArduinoSketch sketch = new ArduinoSketch();

        for(String s : splitData)
        {
            if(s.charAt(0) != ':') // All lines must start with a semicolon
                return null;

            // Get byte count
            String byteCountStr = "0x" + s.substring(1, 3);

            int byteCount = Integer.decode(byteCountStr);

            if(byteCount == 0) // Last line reached. Return sketch object
            {
                return sketch;
            }

            // Get address
            String addressStr = "0x" + s.substring(3, 7);
            int address = Integer.decode(addressStr);

            // Get data
            byte[] data = new byte[byteCount];

            String dataStr = s.substring(9, s.length() -2);

            int i = 0;
            int j = 0;

            while(i < dataStr.length())
            {
                String valueByte = "0x" + dataStr.substring(i, i+2);
                int value = Integer.decode(valueByte);
                data[j] = (byte) value;
                i += 2;
                j++;
            }

            // Checksum validation
            if(!isChecksumOk(hexData))
                return null;

            sketch.putBuffer(address, data);

        }
        return null;
    }

    private static boolean isChecksumOk(String line)
    {
        int checksumValue =  Integer.decode(
                "0x" + line.substring(line.length()-2, line.length()));

        String payload = line.substring(1, line.length()-2);

        int i = 0;
        int value = 0;
        while(i < payload.length())
        {
            String valueByte = "0x" + payload.substring(i, i+2);
            value += Integer.decode(valueByte);
            i += 2;
        }

        value = ~(value) + 1;

        return checksumValue == (value & 0xff);
    }
}
