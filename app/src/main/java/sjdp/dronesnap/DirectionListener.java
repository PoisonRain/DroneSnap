package sjdp.dronesnap;

import android.os.Environment;
import android.util.Log;

import java.io.DataInputStream;
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

    public DirectionListener(InetAddress ip, int port) {
        try {
            mSocketAddress = ip;
            mSocketPort = port;
            mServerSocket = new ServerSocket(port, 100, ip);
            mDirectionResponse = new StringBuilder();
        } catch (Exception e) {
            Log.d(LOG_TAG, "Failed to create a DirectionListener");
            e.printStackTrace();
        }
    }

    public DirectionListener(String ip, int port) {
        try {
            mSocketAddress = InetAddress.getByName(ip);
            mSocketPort = port;
            mServerSocket = new ServerSocket(mSocketPort, 100, mSocketAddress);
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
            mServerSocket.close();
            writeResponseToFile();
            isListening = false;
        }
        catch (Exception e) {
            //already closed
            e.printStackTrace();
        }
    }

    public void getMessage() throws IOException {
        byte[] message = new byte[25];
        DataInputStream dataInputStream = new DataInputStream(mClientSocket.getInputStream());

        try {
            dataInputStream.read(message);
            Log.d(LOG_TAG, "The Response Message: " + message.toString());
            mDirectionResponse.append(message + "\n");

            // If the letter 'f' is received, the destination has been reached.
            if(message[0] == 'f'){ stopListening(); }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeResponseToFile() {
        String storageDir = Environment.getExternalStorageDirectory().toString();
        String filepath = storageDir + "/direction_responses.txt";
        try {
            FileOutputStream fos = new FileOutputStream(filepath);
            fos.write(mDirectionResponse.toString().getBytes(), 0, mDirectionResponse.length());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d(LOG_TAG, "Response file created.");
    }
}
