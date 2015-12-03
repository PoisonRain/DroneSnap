package sjdp.dronesnap;

import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;

/**
 * Created by Samuel Poulton on 12/1/15.
 */
    public class SnapDisbatcher extends Thread {
    static final private String LOG_TAG = "LOG_SEE_ME";

    private LinkedList<byte []> snapUploadList = new LinkedList<byte[]>();
    private boolean isDisbatching = true;
    private Socket socket;
    private static int SERVERPORT = 8150;
    private static String SERVER_IP = "144.39.197.47";

    private byte[] dequeueSnap() {
        return snapUploadList.poll();
    }

    public void addSnaptoUploadList(byte[] snap) {
        snapUploadList.addLast(snap);
    }

    public int listLength(){
        return snapUploadList.size();
    }

    public void stopDisbatching(){
        isDisbatching = false;
    }

    public void run(){
        setUpSocket();

        while(isDisbatching) {
            if(!snapUploadList.isEmpty()) {
                //dequeueSnap()
                //send byte array
                Log.d(LOG_TAG, "current number of snaps in queue: " + listLength());
                sendByteArray(dequeueSnap());
            } else {
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setUpSocket() {
        try {
//            InetAddress serverAddr = InetAddress.getLocalHost();
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
            Log.d(LOG_TAG, "SERVER ADDRESS: " + serverAddr.toString());
            socket = new Socket(serverAddr, SERVERPORT);

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e ) {
            e.printStackTrace();
        }
    }

    private void sendByteArray(byte [] snap) {
        Log.d(LOG_TAG, "sendByteArray to server");

        try {
            OutputStream out = socket.getOutputStream();
            DataOutputStream dataOut = new DataOutputStream(out);
            dataOut.writeInt(snap.length);
            dataOut.write(snap,0, snap.length);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
