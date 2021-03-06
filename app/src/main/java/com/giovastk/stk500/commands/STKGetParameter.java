package com.giovastk.stk500.commands;

import com.giovastk.stk500.responses.STK500Response;
import com.giovastk.stk500.STK500Constants;

import java.nio.ByteBuffer;

/**
 * Get the value of a valid parameter from the STK500 starterkit. If the parameter is not
 * used, the same parameter will be returned together with a Resp_STK_FAILED
 * response to indicate the error. See the parameters section for valid parameters and their
 * meaning.
 */
public class STKGetParameter extends STK500Command
{
    private int parameter;

    public STKGetParameter()
    {
        super(STK500Constants.Cmnd_STK_GET_PARAMETER, 3);
    }

    public STKGetParameter(int parameter)
    {
        super(STK500Constants.Cmnd_STK_GET_PARAMETER, 3);
        this.parameter = parameter;
    }

    @Override
    public byte[] getCommandBuffer()
    {
        ByteBuffer buffer = ByteBuffer.allocate(length);
        buffer.put((byte) commandId);
        buffer.put((byte) parameter);
        buffer.put((byte) STK500Constants.Sync_CRC_EOP);
        return buffer.array();
    }


    @Override
    public STK500Response generateResponse(byte[] buffer) throws Exception {
        if (buffer.length>0) {
            switch(buffer[0]){
                case STK500Constants.Resp_STK_NOSYNC:
                    throw new Exception("NO_SYNC received as first byte in response to "+this.getClass().getSimpleName());

                case STK500Constants.Resp_STK_INSYNC:
                    if (buffer.length>=this.getLength()) {
                        switch(buffer[2]){
                            case STK500Constants.Resp_STK_OK:
                                byte[] dst = new byte[this.getLength()];
                                ByteBuffer.wrap(buffer).get(dst,0,this.getLength());
                                return new STK500Response(commandId,null,dst,true);
                        }
                        throw new Exception("Third byte SHOULD BE STK_OK(0x10)");
                    }
                    return null; // incomplete response, waiting for next reads

                default:
                    throw new Exception("Unknown received as first byte in response to "+this.getClass().getSimpleName());
            }
        }
        throw new Exception("Buffer length SHOULDN'T be zero here");
    }


}
