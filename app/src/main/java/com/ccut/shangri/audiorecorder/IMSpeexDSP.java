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
public class IMSpeexDSP {
    private final static String TAG = "shangri";

    public IMSpeexDSP() {
        init(896);
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

    public native int open(int size);

    public native int denoise(short lin[], int offset, byte encoded[], int size);

    public native void close();

    public static native void file();
}
