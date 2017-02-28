package com.hoho.android.usbserial.util;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Created by Tony on 2/27/14.
 */
public class UsbSerialOutputStream extends OutputStream {

    private UsbSerialPort driver;

    private int timeoutMillis;

    public UsbSerialOutputStream(UsbSerialPort driver) {
        this(driver, 1000);
    }

    public UsbSerialOutputStream(UsbSerialPort driver, int timeoutMillis) {
        this.driver = driver;
        this.timeoutMillis = timeoutMillis;
    }

    @Override
    public void write(int i) throws IOException {
        write(new byte[]{(byte) i});
    }

    @Override
    public void write(byte[] b) throws IOException {
        driver.write(b, timeoutMillis);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        write(Arrays.copyOfRange(b, off, off + len));
    }
}
