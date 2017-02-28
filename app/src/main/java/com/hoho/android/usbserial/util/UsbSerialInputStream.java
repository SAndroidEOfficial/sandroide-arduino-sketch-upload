package com.hoho.android.usbserial.util;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;

import java.io.IOException;
import java.io.InputStream;

public class UsbSerialInputStream extends InputStream {

	private UsbSerialPort driver = null;
	
	private int timeoutMillis;
	
	public UsbSerialInputStream(UsbSerialPort driver) {
		this(driver, 1000);
	}
	
	public UsbSerialInputStream(UsbSerialPort driver, int timeoutMillis) {
		this.driver = driver;
		this.timeoutMillis = timeoutMillis;
	}
	
	@Override
	public int read() throws IOException {
		byte[] buffer = new byte[1];
		return driver.read(buffer, timeoutMillis);
	}
	
	@Override
	public int read(byte[] b) throws IOException {
		return driver.read(b, timeoutMillis);
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		byte[] buffer = new byte[len];
		int n = driver.read(buffer, timeoutMillis);
		if (n > 0) {
			System.arraycopy(buffer, 0, b, off, n);
		}
		return n;
	}
	
	@Override
	public void close() throws IOException {
		driver.close();
	}

}
