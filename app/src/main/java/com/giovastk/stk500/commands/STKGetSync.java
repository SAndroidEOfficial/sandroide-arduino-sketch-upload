package com.giovastk.stk500.commands;

import com.giovastk.stk500.STK500Constants;

import java.nio.ByteBuffer;

/**
 * Use this command to try to regain synchronization when sync is lost. Send this command
 * until Resp_STK_INSYNC is received
 */
public class STKGetSync extends STK500Command
{
    public STKGetSync()
    {
        super(STK500Constants.Cmnd_STK_GET_SYNC, 2);
    }

    @Override
    public byte[] getCommandBuffer()
    {
        ByteBuffer buffer = ByteBuffer.allocate(length);
        buffer.put((byte) commandId);
        buffer.put((byte) STK500Constants.Sync_CRC_EOP);
        return buffer.array();
    }
}
