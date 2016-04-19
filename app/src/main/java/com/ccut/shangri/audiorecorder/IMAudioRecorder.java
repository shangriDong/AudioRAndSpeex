package com.ccut.shangri.audiorecorder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by admin on 2016/4/11.
 */
public class IMAudioRecorder implements Runnable {
    private static final Logger log = Logger.getLogger("IMAudioRecorder.class");
    private final static SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");//设置日期格式
    private int frequency = 8000; //采样频率，每秒钟能够采样的次数
    private int channelConfiguration = AudioFormat.CHANNEL_IN_DEFAULT; //声道设置 MONO单声道
    private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT; //编码格式PCM 16BIT
    private int audioSource = MediaRecorder.AudioSource.MIC; //声音来源MIC
    private boolean isRecording;
    private AudioRecord audioRecord;
    private int mMinBufferSize; //采集数据需要的缓冲区的大小
    private boolean mRun = false;
    private IMSpeexDSPAndEnc mIMSpeexDSPAndEnc;
    private String mAudioAmrFileName = "test";
    private int frameCount = 0;
    private OnUpdateVolumeListener mListener;
    private long mStartTime;
    private long mEndTime;
    private long mLastDurationTime;

    public IMAudioRecorder(OnUpdateVolumeListener l) {
        mIMSpeexDSPAndEnc = new IMSpeexDSPAndEnc();
        mListener = l;
        log.setLevel(Level.INFO);
    }

    @Override
    public void run() {
        if(mRun) {
            return;
        }

        mRun = true;
        try {
            // Create a DataOuputStream to write the audio data into the saved file.
            mIMSpeexDSPAndEnc.init();

            log.info("Recording start");

            // Create a new AudioRecord object to record the audio.
            mMinBufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
            audioRecord = new AudioRecord(audioSource, frequency, channelConfiguration, audioEncoding, mMinBufferSize);

            short[] buffer = new short[mMinBufferSize];
            audioRecord.startRecording();
            mStartTime = System.currentTimeMillis();

            isRecording = true ;
            while (isRecording) {
                int bufferReadResult = audioRecord.read(buffer, 0, mMinBufferSize);
                mIMSpeexDSPAndEnc.denoiseAndEnc(buffer, 0, bufferReadResult); //降噪

                log.info("bufferReadResult = " + bufferReadResult + " mMinBufferSize = " + mMinBufferSize);

                frameCount += bufferReadResult / 160;
                //if (frameCount / 200 == 0) {
                int temp = getVolumeMax(buffer);
                mListener.updateVolumeMax(temp);
                //}
                log.info("volume = " + temp);
            }

            audioRecord.stop();
            audioRecord.release();
            mIMSpeexDSPAndEnc.close();

            mEndTime = System.currentTimeMillis();
            mLastDurationTime = mEndTime - mStartTime;
            log.info("Recording stop, duration = " + mLastDurationTime);
            frameCount = 0;

        } catch (Throwable t) {
            log.info("Recording Failed");
        } finally {

        }

        mRun = false;
    }

    public void setRecording(boolean isRecording) {
        this.isRecording = isRecording;
    }

    private int getVolumeMax(short[] short_buffer) {
        int max = 0;
        if (short_buffer.length > 0) {
            for (int i = 0; i < short_buffer.length; i++) {
                if (Math.abs(short_buffer[i]) > max) {
                    max = Math.abs(short_buffer[i]);
                }
            }
        }
        return max;
    }

    /**
     * 判断是否有外部存储设备sdcard
     *
     * @return true | false
     */
    private static boolean isSdcardExit() {
        if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            return true;
        else
            return false;
    }

    private String getAmrFilePatgh() {
        mAudioAmrFileName = "";
        if (isSdcardExit()) {
            String fileBasePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            mAudioAmrFileName = fileBasePath + "/vvim/" + df.format(new Date()) + ".amr";
        }
        return mAudioAmrFileName;
    }

    public String getFilePath() {
        return mAudioAmrFileName;
    }

    public long getLastDurationTime() {
        return mLastDurationTime/1000 == 0 ?  1L : mLastDurationTime/1000; //单位：秒
    }

    public interface OnUpdateVolumeListener {
        void updateVolumeMax(int volume);
    }
}
