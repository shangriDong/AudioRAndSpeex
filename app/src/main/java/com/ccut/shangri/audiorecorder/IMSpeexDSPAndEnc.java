package com.ccut.shangri.audiorecorder;

import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Created by admin on 2016/4/11.
 */
public class IMSpeexDSPAndEnc {
    private static final Logger log = Logger.getLogger("IMSpeexDSPAndEnc.class");
    private static final int SAMPLING_NUM = 160;
    private static final int FREQUENCY = 8000;
    private static final int CHANNEL = 1;

    public IMSpeexDSPAndEnc() {
        load();
    }

    public void init() {
        init(SAMPLING_NUM, FREQUENCY, CHANNEL);
        log.info("speex opened");
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

    public static native int init(int size, int rate, int channel);

    public static native int denoiseAndEnc(short lin[], int offset, int size);

    public static native void close();
}
