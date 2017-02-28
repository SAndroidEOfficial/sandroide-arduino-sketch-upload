package com.giovastk.stk500.commands;

import com.giovastk.stk500.STK500Constants;

import java.nio.ByteBuffer;

/**
 * Leave programming mode.
 */
public class STKLeaveProgramMode extends STK500Command
{
    public STKLeaveProgramMode()
    {
        super(STK500Constants.Cmnd_STK_LEAVE_PROGMODE, 2);
    }

    @Override
    public byte[] getCommandBuffer()
    {
        ByteBuffer buffer = ByteBuffer.allocate(length);
        buffer.put((byte) STK500Constants.Cmnd_STK_LEAVE_PROGMODE);
        buffer.put((byte) STK500Constants.Sync_CRC_EOP);
        return buffer.array();
    }
}
