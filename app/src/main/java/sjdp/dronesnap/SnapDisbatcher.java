package sjdp.dronesnap;

import android.util.Log;

import java.util.LinkedList;

/**
 * Created by Samuel Poulton on 12/1/15.
 */
public class SnapDisbatcher extends Thread {
    static final private String LOG_TAG = "LOG_SEE_ME";

    private LinkedList<byte []> snapUploadList = new LinkedList<byte[]>();
    private boolean isDisbatching = true;

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

    private void sendByteArray(byte [] snap) {
        //Off it goes!!
        Log.d(LOG_TAG, "sendByteArray to server");
    }
}
