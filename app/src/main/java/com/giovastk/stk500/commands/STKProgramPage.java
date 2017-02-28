package com.giovastk.stk500.commands;

import com.giovastk.stk500.STK500Constants;

import java.nio.ByteBuffer;

/**
 * Download a block of data to the starterkit and program it in FLASH or EEPROM of the
 * current device. The data block size should not be larger than 256 bytes.
 */
public class STKProgramPage extends STK500Command
{
    public static final String EEPROM = "E";
    public static final String FLASH = "F";

    private int bytesHigh;
    private int bytesLow;
    private int memType;
    private byte[] data;

    public STKProgramPage(String memType, byte[] data)
    {
        super(STK500Constants.Cmnd_STK_PROG_PAGE, 2);

        if(memType.equals(EEPROM))
            this.memType = EEPROM.getBytes()[0];
        else
            this.memType = FLASH.getBytes()[0];

        this.bytesHigh = (data.length >> 8) & 0xff;
        this.bytesLow = data.length & 0xff;
        this.data = data;
    }

    @Override
    public byte[] getCommandBuffer()
    {
        ByteBuffer buffer = ByteBuffer.allocate(data.length + 5);
        buffer.put((byte) STK500Constants.Cmnd_STK_PROG_PAGE);
        buffer.put((byte) bytesHigh);
        buffer.put((byte) bytesLow);
        buffer.put((byte) memType);
        buffer.put(data);
        buffer.put((byte) STK500Constants.Sync_CRC_EOP);
        return buffer.array();
    }

}
