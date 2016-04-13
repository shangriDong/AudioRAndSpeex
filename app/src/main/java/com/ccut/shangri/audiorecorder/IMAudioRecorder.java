package com.ccut.shangri.audiorecorder;

import android.media.AmrInputStream;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;

/**
 * Created by admin on 2016/4/11.
 */
public class IMAudioRecorder implements Runnable {

    private static final String TAG = "Shangri";
    private int frequency = 11025; //采样频率，每秒钟能够采样的次数
    private int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO; //声道设置 MONO单声道
    private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT; //编码格式PCM 16BIT
    private int audioSource = MediaRecorder.AudioSource.MIC; //声音来源MIC
    private boolean isRecording;
    private AudioRecord audioRecord;
    private int mMinBufferSize; //采集数据需要的缓冲区的大小
    private boolean mRun = false;
    private List<RecordPCMData> mPcmList;
    private Object mPcmMutex;
    private int encoder_packagesize = 1024;
    private int mPcmCount;
    private IMSpeexDSP mIMSpeexDSP = null;

    public IMAudioRecorder(List<RecordPCMData> l, Object mutex, IMSpeexDSP IMSpeexDSP) {
        mPcmList = l;
        mPcmMutex = mutex;
        this.mIMSpeexDSP = IMSpeexDSP;
    }

    @Override
    public void run() {
        //MediaRecorder a = new MediaRecorder();
        //a.setAudioSource(1);
        if(mRun) {
            return;
        }

        mRun = true;
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/reverseme.pcm");
        OutputStream os = null;
        DataOutputStream dos = null;
        // Delete any previous recording.
        if (file.exists())
            file.delete();

        // Create the new file.
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create " + file.toString());
        }

        try {
            // Create a DataOuputStream to write the audio data into the saved file.
            os = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(os);
            dos = new DataOutputStream(bos);

            // Create a new AudioRecord object to record the audio.
            mMinBufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
            audioRecord = new AudioRecord(audioSource, frequency, channelConfiguration, audioEncoding, mMinBufferSize);
            //MediaRecorder.AudioEncoder
            short[] buffer = new short[mMinBufferSize];
            audioRecord.startRecording();

            isRecording = true ;
            while (isRecording) {
                int bufferReadResult = audioRecord.read(buffer, 0, mMinBufferSize);
                for (int i = 0; i < bufferReadResult; i++) {
                    dos.writeShort(buffer[i]);

                }
                mPcmCount++;
                RecordPCMData rd = new RecordPCMData();
                synchronized (mPcmMutex) {
                    rd.size = bufferReadResult;
                    rd.ready = buffer;
                    mPcmList.add(rd);
                }
                Log.d(TAG, "bufferReadResult = " + bufferReadResult + "mMinBufferSize = " + mMinBufferSize);
            }

            audioRecord.stop();
            audioRecord.release();
            dos.close();

            mIMSpeexDSP.setProcessDenoiseFlag(false);

        } catch (Throwable t) {
            Log.e(TAG, "Recording Failed");
        } finally {

        }

        mRun = false;
    }

    public void setRecording(boolean isRecording) {
        this.isRecording = isRecording;
    }

    class RecordPCMData {
        protected int size;
        protected short[] ready = new short[encoder_packagesize];
    }
}
