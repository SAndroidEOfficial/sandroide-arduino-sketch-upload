package com.giovastk.stk500.arduino;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An Arduino sketch in binary format
 */
public class ArduinoSketch
{
    private List<byte[]> data;
    private List<Integer> addresses;

    public ArduinoSketch()
    {
        data = new ArrayList<>();
        addresses = new ArrayList<>();
    }

    public void putBuffer(int offset, byte[] buffer)
    {
        data.add(buffer);
        addresses.add(offset);
    }

    public int getCount()
    {
        return data.size();
    }

    public byte[] getData(int index)
    {
        return data.get(index);
    }

}
