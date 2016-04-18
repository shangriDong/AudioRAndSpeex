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

/**
 * Created by admin on 2016/4/11.
 */
public class IMAudioRecorder implements Runnable {
    private final static SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");//设置日期格式
    private static final String TAG = "Shangri";
    private int frequency = 8000; //采样频率，每秒钟能够采样的次数
    private int channelConfiguration = AudioFormat.CHANNEL_IN_DEFAULT; //声道设置 MONO单声道
    private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT; //编码格式PCM 16BIT
    private int audioSource = MediaRecorder.AudioSource.MIC; //声音来源MIC
    private boolean isRecording;
    private AudioRecord audioRecord;
    private int mMinBufferSize; //采集数据需要的缓冲区的大小
    private boolean mRun = false;
    private IMSpeexDSPAndEnc mIMSpeexDSPAndEnc;
    //private AmrEncoder mAmrEncoder;
    private String mAudioAmrFileName = "testa";
    public IMAudioRecorder() {
        mIMSpeexDSPAndEnc = new IMSpeexDSPAndEnc();
        //mAmrEncoder = new AmrEncoder();
    }

    @Override
    public void run() {
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
            mIMSpeexDSPAndEnc.init();

            os = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(os);
            dos = new DataOutputStream(bos);

            Log.i("shangri", "-----------------------------start");

            // Create a new AudioRecord object to record the audio.
            mMinBufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
            audioRecord = new AudioRecord(audioSource, frequency, channelConfiguration, audioEncoding, mMinBufferSize);

            short[] buffer = new short[mMinBufferSize];
            audioRecord.startRecording();

            isRecording = true ;

            while (isRecording) {
                int bufferReadResult = audioRecord.read(buffer, 0, mMinBufferSize);
                for (int i = 0; i < bufferReadResult; i++) {
                    dos.writeShort(buffer[i]);
                }
                Log.d(TAG, "bufferReadResult = " + bufferReadResult + " mMinBufferSize = " + mMinBufferSize);

                //short[] pcm = new short[bufferReadResult];

                mIMSpeexDSPAndEnc.denoiseAndEnc(buffer, 0, null, bufferReadResult); //降噪
            }

            audioRecord.stop();
            audioRecord.release();
            dos.close();
            mIMSpeexDSPAndEnc.close();
            Log.i("shangri", "-----------------------------end");

        } catch (Throwable t) {
            Log.e(TAG, "Recording Failed");
        } finally {

        }

        mRun = false;
    }

    public void setRecording(boolean isRecording) {
        this.isRecording = isRecording;
    }

    public int getVolumeMax(short[] short_buffer) {
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
}
