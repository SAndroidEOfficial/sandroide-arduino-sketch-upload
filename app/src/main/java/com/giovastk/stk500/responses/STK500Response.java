package com.giovastk.stk500.responses;

import com.hoho.android.usbserial.util.HexDump;

public class STK500Response
{
    private int commandId;
    private int[] parameters;
    private byte[] data;
    private boolean ok;

    public STK500Response(int commandId, int[] parameters, byte[] data, boolean ok)
    {
        this.commandId = commandId;
        this.parameters = parameters;
        this.data = data;
        this.ok = ok;
    }

    public int getCommandId()
    {
        return commandId;
    }

    public void setCommandId(int commandId)
    {
        this.commandId = commandId;
    }

    public int[] getParameters()
    {
        return parameters;
    }

    public void setParameters(int[] parameters)
    {
        this.parameters = parameters;
    }

    public byte[] getData()
    {
        return data;
    }

    public void setData(byte[] data)
    {
        this.data = data;
    }

    public boolean isOk()
    {
        return ok;
    }

    public void setOk(boolean ok)
    {
        this.ok = ok;
    }

    @Override
    public String toString() {
        String strp="",str = String.format("{command:%d(0x%s): parameters:[",commandId, HexDump.toHexString(commandId));
        if (parameters!=null) {
            for (int a : parameters) {
                strp += a + " ";
            }
        }
        str += String.format("%s],data:%s,ok:%s}",strp.trim(),HexDump.dumpHexString(data),ok?"true":"false");
        return str;
    }
}
