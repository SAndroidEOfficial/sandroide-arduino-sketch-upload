package com.giovastk.stk500.commands;

import com.giovastk.stk500.responses.STK500Response;
import com.giovastk.stk500.STK500Constants;
import com.giovastk.stk500.STKCallback;
import com.giovastk.stk500.STKCommunicator;

import java.nio.ByteBuffer;

public abstract class STK500Command
{
    protected int commandId;
    protected int length;

    public STK500Command(int commandId, int length)
    {
        this.commandId = commandId;
        this.length = length;
    }

    public abstract byte[] getCommandBuffer();

    public int getLength()
    {
        return length;
    }

    public int getCommandId()
    {
        return commandId;
    }

    public STK500Response generateResponse(byte[] buffer) throws Exception {
        if (buffer.length>0) {
            switch(buffer[0]){
                case STK500Constants.Resp_STK_NOSYNC:
                    throw new Exception("NO_SYNC received as first byte in response to "+this.getClass().getSimpleName());

                case STK500Constants.Resp_STK_INSYNC:
                    if (buffer.length>=this.getLength()) {
                        if (buffer[1]==STK500Constants.Resp_STK_OK) {
                            byte[] dst = new byte[this.getLength()];
                            ByteBuffer.wrap(buffer).get(dst,0,this.getLength());
                            return new STK500Response(commandId,null,dst,true);
                        }
                        throw new Exception("Second byte SHOULD BE STK_OK(0x10)");
                    }
                    return null; // incomplete response, waiting for next reads

                default:
                    throw new Exception("Unknown received as first byte in response to "+this.getClass().getSimpleName());

            }
        }
        throw new Exception("Buffer length SHOULDN'T be zero here");
    }

    public void send(STKCallback cbk) {
        if(STKCommunicator.allowNewCommand.get())
        {
            STKCommunicator.allowNewCommand.set(false);
            STKCommunicator.currentCommand = this;
            STKCommunicator.currentCallback = cbk;
            STKCommunicator.send(getCommandBuffer());
        }
    };

}
