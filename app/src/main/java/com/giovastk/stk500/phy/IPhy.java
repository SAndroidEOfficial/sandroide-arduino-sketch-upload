package com.giovastk.stk500.phy;

import com.giovastk.stk500.responses.STK500Response;

/**
 * Interface to implement classes which define different hardware interfaces like USB, Bluetooth or Wifi
 */
public interface IPhy
{
    void open();
    void write(byte[] data);
    void close();

    interface OnChangesFromPhyLayer
    {
        STK500Response onDataReceived(byte[] data) throws Exception;
    }
}
