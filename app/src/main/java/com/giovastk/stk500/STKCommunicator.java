package com.giovastk.stk500;

import android.os.Handler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import com.giovastk.MainActivity;
import com.giovastk.stk500.commands.*;
import com.giovastk.stk500.phy.IPhy;
import com.giovastk.stk500.responses.*;
import com.hoho.android.usbserial.driver.UsbSerialPort;

/**
 * STK500v1 api
 */
public class STKCommunicator implements IPhy.OnChangesFromPhyLayer
{
    private final String TAG = "GIOVA-STK-"+STKCommunicator.class.getSimpleName();

    public static UsbSerialPort phyComm;
    public static AtomicBoolean allowNewCommand;
    public static STK500Command currentCommand;
    public static STKCallback currentCallback;

    private static Runnable enableSend =  new Runnable() {
        @Override
        public void run() {
            STKCommunicator.allowNewCommand.set(true);
        }
    };

    private static Handler resetHandler = new Handler();

    // Usb constructor
    public STKCommunicator(UsbSerialPort sPort)
    {
        phyComm = sPort;
        allowNewCommand = new AtomicBoolean(true);
    }

    /**
     * Public api
     */
    public static int send(byte[] buff){
        resetHandler.removeCallbacks(enableSend);
        resetHandler.postDelayed(enableSend, 1500);

        int ret= 0;
        try {
            ret = phyComm.write(buff, MainActivity.TIME_WRITE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }


    private static final int MAX_BUFFER = 256 + 5; // Max command length
    private byte[] buffer;

    @Override
    public STK500Response onDataReceived(byte[] dataReceived) throws Exception
    {
        STK500Response rsp = null;
        if (currentCommand!=null && dataReceived.length>0) {

            if (buffer==null) {
                buffer = ByteBuffer.wrap(dataReceived).array();
            } else {
                ByteBuffer newb = ByteBuffer.allocate(buffer.length+dataReceived.length);
                newb = newb.put(buffer);
                newb = newb.put(dataReceived);
                buffer=newb.array();
            }

            rsp = currentCommand.generateResponse(buffer);
            if (rsp != null) {
                int rspDataLength=rsp.getData().length;

                if (buffer.length == rspDataLength) {
                    buffer = null;
                } else {
                    ByteBuffer readBuffer = ByteBuffer.wrap(buffer);
                    readBuffer.position(rspDataLength);
                    buffer = readBuffer.slice().array();
                }

                currentCommand=null;
                allowNewCommand.set(true);
                currentCallback.callbackCall(rsp);
            }
        }
        return rsp;
    }
}
