package com.giovastk;

import android.content.Context;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.giovastk.stk500.STK500Constants;
import com.giovastk.stk500.arduino.ArduinoSketch;
import com.giovastk.stk500.arduino.IntelHexParser;
import com.giovastk.stk500.commands.STKEnterProgMode;
import com.giovastk.stk500.commands.STKGetParameter;
import com.giovastk.stk500.commands.STKGetSync;
import com.giovastk.stk500.commands.STKLoadAddress;
import com.giovastk.stk500.commands.STKProgramPage;
import com.giovastk.stk500.commands.STKSetDevice;
import com.giovastk.stk500.responses.STK500Response;
import com.giovastk.stk500.STKCallback;
import com.giovastk.stk500.STKCommunicator;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.HexDump;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cz.jaybee.intelhex.IntelHexException;
import cz.jaybee.intelhex.Parser;
import cz.jaybee.intelhex.listeners.RangeDetector;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "GIOVA-STK-"+MainActivity.class.getSimpleName();

    private UsbSerialPort sPort = null;

    private TextView mDumpTextView;
    private ScrollView mScrollView;
    private STKCommunicator stk;

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private SerialInputOutputManager mSerialIoManager;

    private int addr = 0, page_size=128, a_div = 2;
    private ByteBuffer sketchBuffer = null;
    private String selectedSketch="200";
    private int uploadCount = 0;

    protected int n=0;

    private final SerialInputOutputManager.Listener mListener =
        new SerialInputOutputManager.Listener() {

            @Override
            public void onRunError(Exception e) {
                Log.d(TAG, "Runner stopped.");
            }

            @Override
            public void onNewData(final byte[] data) {
                String currentCommand = "";
                if (STKCommunicator.currentCommand!=null) {
                    currentCommand  = STKCommunicator.currentCommand.getClass().getSimpleName();
                    //printLogLine("STKCommunicator.currentCommand: " + currentCommand);
                }
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.this.updateReceivedData(data);
                    }
                });
                try {
                    if (stk!=null) {
                        STK500Response rsp = stk.onDataReceived(data);
                        if (rsp != null) printLogLine("Response parsed correctly for command: " + currentCommand);
                    }
                } catch(Exception e) {
                    printLogLine(e.toString());
                    e.printStackTrace();
                    //Log.e(TAG,e.toString());
                }
            }
        };

    protected void loadAddrAndWritePage(){
        final byte[] tosend = new byte[page_size];
        sketchBuffer.get(tosend,0,Math.min(page_size,sketchBuffer.remaining()));

        printLogLine(String.format("Tosend(%d): %s",tosend.length, HexDump.dumpHexString(tosend)));

        if (addr < n) {
            new STKLoadAddress(addr/a_div).send(new STKCallback() {
                @Override

                public void callbackCall(STK500Response rsp) {
                    printLogLine("LOAD ADDRESS: " + rsp.toString());
                    if (rsp.isOk()) {
                        new STKProgramPage("F",tosend).send(new STKCallback() {
                            @Override
                            public void callbackCall(STK500Response rsp) {
                                printLogLine("PROGRAM PAGE: " + rsp.toString());
                                if (rsp.isOk()) {
                                    printLogLine(String.format("%d%% UPLOAD PROGRESS addr:%d n:%d", n>0?Math.round(addr/(double)n*100):-1, addr, n));
                                    loadAddrAndWritePage();
                                }
                            }
                        });
                    }
                }
            });
            addr+=page_size;
        } else {
            printLogLine("FINISH UPLOADING " + selectedSketch);
        }
    };


    protected ByteArrayOutputStream uploadSketchJavaIntelHexPaser() {
        selectedSketch =String.format("blink%s.hex",true?"200":"1000");
        //String.format("blink%s.hex",uploadCount++%2==0?"200":"1000");

        final ByteArrayOutputStream bo = new ByteArrayOutputStream();
        byte[] binaryFile=null;
        try (InputStream is = getAssets().open(selectedSketch)) {
            Parser ihp = new Parser(getAssets().open(selectedSketch));
            ihp.setDataListener(new RangeDetector() {
                @Override
                public void data(long address, byte[] data) throws IOException {
                    // process data
                    bo.write(data);
                }
                @Override
                public void eof() {
                    // do some action
                }
            });
            ihp.parse();
        } catch (IntelHexException | IOException ex) {
            ex.printStackTrace();
        }

        binaryFile = bo.toByteArray();
        sketchBuffer = ByteBuffer.wrap(binaryFile);

        int page_size = 128, n_bytes = binaryFile.length;

        if ((n_bytes % page_size) != 0) {
            n = n_bytes + page_size - (n_bytes % page_size);
        } else {
            n = n_bytes;
        }

        printLogLine(String.format("Start writing %d bytes",binaryFile.length));

        addr = 0;
        //loadAddrAndWritePage();  // COMMENTED to test new intel hex parser
        return bo;
    }

    protected ByteArrayOutputStream uploadSketch() {
        selectedSketch =String.format("blink%s.hex",true?"200":"1000");
                //String.format("blink%s.hex",uploadCount++%2==0?"200":"1000");

        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        byte[] binaryFile=null;
        try {
            HexFileParser parser = new HexFileParser(getAssets().open(selectedSketch));
            ArrayList<HexFileParser.Record> records = parser.getRecords();

            int len=0;
            for (HexFileParser.Record r : records) {
                len+=r.data.length;
                bo.write(r.data); // ,bo.size(),r.data.length
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        binaryFile = bo.toByteArray();
        sketchBuffer = ByteBuffer.wrap(binaryFile);

        int page_size = 128, n_bytes = binaryFile.length;

        if ((n_bytes % page_size) != 0) {
            n = n_bytes + page_size - (n_bytes % page_size);
        } else {
            n = n_bytes;
        }

        printLogLine(String.format("Start writing %d bytes",binaryFile.length));

        addr = 0;
        //loadAddrAndWritePage(); // COMMENTED to test new intel hex parser
        return bo;
    }


    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    protected ByteArrayOutputStream uploadSketchOriginalLibrary() {
        selectedSketch =String.format("blink%s.hex",true?"200":"1000");
        //String.format("blink%s.hex",uploadCount++%2==0?"200":"1000");

        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        byte[] binaryFile=null;
        try {
            String hexStr = convertStreamToString(getAssets().open(selectedSketch));
            ArduinoSketch sk = IntelHexParser.parseHexFile(hexStr);
            for (int i=0; i<sk.getCount();i++) {
                bo.write(sk.getData(i));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        binaryFile = bo.toByteArray();
        sketchBuffer = ByteBuffer.wrap(binaryFile);

        int page_size = 128, n_bytes = binaryFile.length;

        if ((n_bytes % page_size) != 0) {
            n = n_bytes + page_size - (n_bytes % page_size);
        } else {
            n = n_bytes;
        }

        printLogLine(String.format("Start writing %d bytes",binaryFile.length));

        addr = 0;
        //loadAddrAndWritePage(); // COMMENTED to test new intel hex parser
        return bo;
    }

    protected void programAndUpload(){
        try {
            printLogLine(String.format("Setting DTR and RTS to low"));
            sPort.setDTR(false);
            sPort.setRTS(false);
            Thread.sleep(50);

            long startTime = System.currentTimeMillis();

            printLogLine(String.format("Setting DTR and RTS to high"));
            sPort.setDTR(true);
            sPort.setRTS(true);
            Thread.sleep(300);

            new STKGetSync().send(new STKCallback() {
                @Override
                public void callbackCall(STK500Response rsp) {
                    printLogLine(rsp.toString());
                    if (rsp.isOk()) {
                        new STKGetParameter(STK500Constants.Parm_STK_SW_MAJOR).send(new STKCallback() {
                            @Override
                            public void callbackCall(STK500Response rsp) {
                                printLogLine("Major GET Result:"+rsp.toString());
                                if (rsp.isOk()) new STKGetParameter(STK500Constants.Parm_STK_SW_MINOR).send(new STKCallback() {
                                    @Override
                                    public void callbackCall(STK500Response rsp) {
                                        printLogLine("Minor GET Result:"+rsp.toString());
                                        if (rsp.isOk()) new STKSetDevice().send(new STKCallback() {
                                            @Override
                                            public void callbackCall(STK500Response rsp) {
                                                printLogLine("SET Device Result: "+rsp.toString());
                                                if (rsp.isOk()) new STKEnterProgMode().send(new STKCallback() {
                                                    @Override
                                                    public void callbackCall(STK500Response rsp) {
                                                        printLogLine("Enter Prog Mode Result: "+rsp.toString());
                                                        if (rsp.isOk()) uploadSketch();
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }

                }
            });

        } catch(Exception ex) {
            printLogLine(ex.getMessage());
        }
    };

    class STKTask extends AsyncTask<String, Void, Integer> {
        /** The system calls this to perform work in a worker thread and
         * delivers it the parameters given to AsyncTask.execute() */
        protected Integer doInBackground(String... urls) {

            if (sPort!=null) {

                programAndUpload();

            }

            return -1;
        }

        /** The system calls this to perform work in the UI thread and delivers
         * the result from doInBackground() */
        protected void onPostExecute() {
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDumpTextView = (TextView) findViewById(R.id.consoleText);
        mScrollView = (ScrollView) findViewById(R.id.demoScroller);

        Button buttonClear = (Button) findViewById(R.id.btnClear);
        Button buttonFlash = (Button) findViewById(R.id.btnFlash);

        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (sPort!=null) {
                        mDumpTextView.setText("");
                   }
                } catch(Exception e) {
                    Log.e(TAG,e.getMessage());
                }
            }
        });

        buttonFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ByteArrayOutputStream bo1,bo2,bo3;

                bo1=uploadSketch(); // OK WORKS
                bo3=uploadSketchJavaIntelHexPaser(); // OK WORKS
                bo2=uploadSketchOriginalLibrary();   // NOT OK; il tipo non l'ha mai testata. La funzione checksum nella libreria crasha
            }
        });

    }

    private void updateReceivedData(byte[] data) {
        final String message = "Read " + data.length + " bytes: \n"
            + HexDump.dumpHexString(data) + "\n\n";
        mDumpTextView.append(message);
        mScrollView.smoothScrollTo(0, mDumpTextView.getBottom());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onPause() {
        super.onPause();
        stopIoManager();
        if (sPort != null) {
            try {
                sPort.close();
            } catch (IOException e) {
                // Ignore.
            }
            sPort = null;
        }
        finish();
    }


    @Override
    protected void onResume() {
        super.onResume();
        try {

            // Find all available drivers from attached devices.
            UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
            List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
            if (availableDrivers.isEmpty()) {
                throw new Exception("Can't find any usb drivers attached!");
            }

            // Open a connection to the first available driver.
            UsbSerialDriver driver = availableDrivers.get(0);
            UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
            if (connection == null) {
                throw new Exception("Can't open connection to first available driver!");
            }

            // Read some data! Most have just one port (port 0).
            List<UsbSerialPort> ports = driver.getPorts();
            if (ports.size() <= 0) throw new Exception("Can't find any USB serial ports on selected driver!");
            sPort = ports.get(0);
            try {
                sPort.open(connection);
            } catch (Exception e) {
                throw new Exception("Can't open first available port on first available driver!");
            }
            try {
                //sPort.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
                //sPort.setParameters(19200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
                sPort.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

                showStatus(mDumpTextView, "CD  - Carrier Detect", sPort.getCD());
                showStatus(mDumpTextView, "CTS - Clear To Send", sPort.getCTS());
                showStatus(mDumpTextView, "DSR - Data Set Ready", sPort.getDSR());
                showStatus(mDumpTextView, "DTR - Data Terminal Ready", sPort.getDTR());
                showStatus(mDumpTextView, "DSR - Data Set Ready", sPort.getDSR());
                showStatus(mDumpTextView, "RI  - Ring Indicator", sPort.getRI());
                showStatus(mDumpTextView, "RTS - Request To Send", sPort.getRTS());

            } catch (Exception e) {
                sPort.close();
                sPort = null;
                throw e;
            } finally {
            }
        } catch(Exception e) {
            Log.e(TAG,e.getMessage());
        }
        onDeviceStateChange();
    }

    public void printLogLine(String line){
        final String out = line;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDumpTextView.append(out+"\n");
            }
        });
    }

    private void showStatus(TextView theTextView, String theLabel, boolean theValue){
        String msg = theLabel + ": " + (theValue ? "enabled" : "disabled") + "\n";
        theTextView.append(msg);
    }

    private void stopIoManager() {
        if (mSerialIoManager != null) {
            Log.i(TAG, "Stopping io manager ..");
            mSerialIoManager.stop();
            mSerialIoManager = null;
            stk = null;
        }
    }

    private void startIoManager() {
        if (sPort != null) {
            Log.i(TAG, "Starting io manager ..");
            mSerialIoManager = new SerialInputOutputManager(sPort, mListener);
            mExecutor.submit(mSerialIoManager);
            stk = new STKCommunicator(sPort);
        }
    }

    private void onDeviceStateChange() {
        stopIoManager();
        startIoManager();
    }

}
