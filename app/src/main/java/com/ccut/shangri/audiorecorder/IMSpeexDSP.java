package com.ccut.shangri.audiorecorder;

import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

/**
 * Created by admin on 2016/4/11.
 */
public class IMSpeexDSP implements Runnable {
    private final static String TAG = "Shangri";
    private List<IMAudioRecorder.RecordPCMData> mPcmList;
    private Object mPcmMutex;
    private Object mAmrMutext;
    private boolean isProcessDenoiseFlag = true;
    public static int encoder_packagesize = 1024;
    private int processPcmCount;
    private AmrEncoder mAmrEncoder;

    public IMSpeexDSP(List<IMAudioRecorder.RecordPCMData> l, Object mutex) {
        mPcmList = l;
        mPcmMutex = mutex;
    }
    public void init(int size) {
        load();
        open(size);
        Log.d(TAG, "speex opened");
    }

    public void destory() {
        close();
    }

    private void load() {
        try {
            System.loadLibrary("speexdsp");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        while (true) {
            if (mPcmList.size() == 0) {
                Log.d(TAG, "no data need to do encode");
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            if (mPcmList.size() > 0) {
                byte[] processedData = new byte[encoder_packagesize];
                synchronized (mPcmMutex) {
                    IMAudioRecorder.RecordPCMData rawdata = mPcmList.remove(0);
                    if(denoise(rawdata.ready, 0, processedData, rawdata.size) != -1) {

                    }
                }

                /*InputStream sbs = new ByteArrayInputStream(processedData);
                String rootPath = Environment.getExternalStorageDirectory().getPath();
                String amrPath = rootPath + "/test.amr";
                mAmrEncoder.pcm2Amr(sbs, amrPath);*/
                processPcmCount++;
                if (!isProcessDenoiseFlag && processPcmCount >= 1000) {
                    break;
                }
            }
        }
    }

    public void setProcessDenoiseFlag(boolean flag) {
        this.isProcessDenoiseFlag = flag;
    }

    public int getProcessPcmCount() {
        return this.processPcmCount;
    }

    public native int open(int size);
    public native int denoise(short lin[], int offset, byte encoded[], int size);
    public native void close();
}
