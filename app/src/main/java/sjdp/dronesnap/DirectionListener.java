package sjdp.dronesnap;

import android.os.Environment;
import android.util.Log;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Samuel Poulton on 12/3/15.
 * DirectionListener awaits to receive direction messages.
 * If the thread is killed or the letter 'f' is received,
 * the results are written to a file.
 */
public class DirectionListener extends Thread {
    static final private String LOG_TAG = "LOG_SEE_ME";

    private ServerSocket mServerSocket = null;
    private Socket mClientSocket = null;
    private InetAddress mSocketAddress;
    private int mSocketPort;
    private boolean isListening = true;
    private StringBuilder mDirectionResponse;
    private String mResponseFilename;

    /*
    The flightname is used in the naming of the response file.
     */
    public DirectionListener(String ip, int port, String flightname) {
        try {
            mSocketAddress = InetAddress.getByName(ip);
            mSocketPort = port;
            mServerSocket = new ServerSocket(mSocketPort, 100, mSocketAddress);
            mDirectionResponse = new StringBuilder();
            if(!flightname.isEmpty())
                mResponseFilename = flightname;
            else
                mResponseFilename = "direction_responses";
        } catch (Exception e) {
            Log.d(LOG_TAG, "Failed to create a DirectionListener");
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            listen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void listen() throws IOException, InterruptedException {
        Log.d(LOG_TAG, "Listening on: " + mSocketAddress + ":" + mSocketPort);
        while(isListening) {
            mClientSocket = mServerSocket.accept();
            getMessage();
        }
    }

    public void stopListening() {
        try {
            isListening = false;
            mServerSocket.close();
            writeResponseToFile();
            interrupt();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getMessage() throws IOException {
        Log.d(LOG_TAG, "Attempting to getMessage");
        byte[] directionResponse = new byte[25];
        DataInputStream dataInputStream = new DataInputStream(mClientSocket.getInputStream());

        try {
            dataInputStream.read(directionResponse);
            String responseString = new String(directionResponse).trim();
            Log.d(LOG_TAG, "The Response Message: " + responseString);
            mDirectionResponse.append(responseString + "\n");

            // If the letter 'f' is received, the destination has been reached.
//            if(responseString.charAt(0) == 'f'){ stopListening(); }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeResponseToFile() {
        String storageDir = Environment.getExternalStorageDirectory().toString();
        String filepath = storageDir + "/" + mResponseFilename + ".txt";
        try {
            File responseFile = new File(filepath);
            FileOutputStream fos = new FileOutputStream(responseFile, true);

            if(responseFile.exists()) {
                String BREAKER = "---------------------------\n";
                fos.write(BREAKER.getBytes(), 0, BREAKER.length());
            }

            fos.write(mDirectionResponse.toString().getBytes(), 0, mDirectionResponse.length());
            fos.close();
            Log.d(LOG_TAG, "Response file created.");
        } catch (Exception e) {
            Log.d(LOG_TAG, "Response file failed to be created");
            e.printStackTrace();
        }
    }
}
