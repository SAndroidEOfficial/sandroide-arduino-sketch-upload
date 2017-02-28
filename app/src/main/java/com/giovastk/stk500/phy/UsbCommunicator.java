package com.giovastk.stk500.phy;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;

/**
 * Usb frontend for UsbSerial lib
 */
public class UsbCommunicator implements IPhy
{
    private IPhy.OnChangesFromPhyLayer callback;

    private UsbDevice device;
    private UsbDeviceConnection connection;

    public UsbCommunicator(UsbDevice device, UsbDeviceConnection connection)
    {
        this.device = device;
        this.connection = connection;
    }

    @Override
    public void open()
    {

    }

    @Override
    public void write(byte[] data)
    {

    }

    @Override
    public void close()
    {

    }
}
