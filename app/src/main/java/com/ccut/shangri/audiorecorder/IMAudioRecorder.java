package com.ccut.shangri.audiorecorder;

import android.media.AmrInputStream;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

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
    private IMSpeexDSP mIMSpeexDSP;
    private AmrEncoder mAmrEncoder;
    private String mAudioAmrFileName = "testa";
    public IMAudioRecorder() {
        mIMSpeexDSP = new IMSpeexDSP();
        mAmrEncoder = new AmrEncoder();
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
            os = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(os);
            dos = new DataOutputStream(bos);

            // Create a new AudioRecord object to record the audio.
            mMinBufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
            audioRecord = new AudioRecord(audioSource, frequency, channelConfiguration, audioEncoding, mMinBufferSize);
            //MediaRecorder.AudioEncoder
            short[] buffer = new short[mMinBufferSize];
            byte[] pcmSpeexBuffer = new byte[mMinBufferSize * 2];
            audioRecord.startRecording();

            isRecording = true ;

            mAmrEncoder.pcm2AmrHeader(getAmrFilePatgh());

            while (isRecording) {
                int bufferReadResult = audioRecord.read(buffer, 0, mMinBufferSize);
                for (int i = 0; i < bufferReadResult; i++) {
                    dos.writeShort(buffer[i]);
                }
                Log.d(TAG, "bufferReadResult = " + bufferReadResult + "mMinBufferSize = " + mMinBufferSize);

                //mIMSpeexDSP.denoise(buffer, 0, pcmSpeexBuffer, bufferReadResult); //降噪

                mAmrEncoder.pcm2AmrProess(new ByteArrayInputStream(pcmSpeexBuffer), getFilePath()); //压缩amr

            }

            audioRecord.stop();
            audioRecord.release();
            dos.close();

            mIMSpeexDSP.close();

        } catch (Throwable t) {
            Log.e(TAG, "Recording Failed");
        } finally {

        }

        mRun = false;
    }

    public void setRecording(boolean isRecording) {
        this.isRecording = isRecording;
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
