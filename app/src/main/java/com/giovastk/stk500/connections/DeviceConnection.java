package com.giovastk.stk500.connections;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Tony on 2/24/14.
 */
public interface DeviceConnection {
    public void requestPermission(Runnable granted);
    public void open() throws IOException;
    public Double getMeasure() throws IOException;
    public void close();
    public InputStream getInput();
    public OutputStream getOutput();

}
