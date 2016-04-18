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
public class IMSpeexDSPAndEnc {
    private final static String TAG = "shangri";

    public IMSpeexDSPAndEnc() {
        init();
    }

    public void init() {
        load();
        init(160, 8000);
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

    public static native int init(int size, int rate);

    public static native int denoiseAndEnc(short lin[], int offset, short encoded[], int size);

    public static native void close();

    public static native void file();
}
