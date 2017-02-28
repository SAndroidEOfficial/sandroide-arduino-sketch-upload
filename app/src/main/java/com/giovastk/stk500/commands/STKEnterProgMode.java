package com.giovastk.stk500.commands;

import com.giovastk.stk500.STK500Constants;
import com.giovastk.stk500.STKCallback;
import com.giovastk.stk500.STKCommunicator;

import java.nio.ByteBuffer;

/**
 * Enter Programming mode for the selected device. The Programming mode and device
 *programming parameters must have been set by Cmnd_STK_SET_DEVICE prior to
 *calling this command, or the command will fail with a Resp_STK_NODEVICE response.
 */

public class STKEnterProgMode extends STK500Command
{
    public STKEnterProgMode()
    {
        super(STK500Constants.Cmnd_STK_ENTER_PROGMODE, 2);
    }

    @Override
    public byte[] getCommandBuffer()
    {
        ByteBuffer buffer = ByteBuffer.allocate(length);
        buffer.put((byte) STK500Constants.Cmnd_STK_ENTER_PROGMODE);
        buffer.put((byte) STK500Constants.Sync_CRC_EOP);
        return buffer.array();
    }

    @Override
    public void send(STKCallback cbk) {
        if(STKCommunicator.allowNewCommand.get())
        {
            STKCommunicator.allowNewCommand.set(false);
            STKCommunicator.currentCommand = this;
            STKCommunicator.currentCallback = cbk;
            STKCommunicator.send(getCommandBuffer());
        }
    }

}
