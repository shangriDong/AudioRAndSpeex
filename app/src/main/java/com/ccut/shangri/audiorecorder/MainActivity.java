package com.ccut.shangri.audiorecorder;

import android.app.Activity;
import android.media.AmrInputStream;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends Activity {

    private Button mStartRecordBtn;
    private Button mPlayPcmBtn;
    private Button mDenoiseAndPlayAmrBtn;
    private Button mStopRecordBtn;
    private IMAudioRecorder mAudioRecorder;
    private IMAudioTrack mAudioTrack;
    private List<IMAudioRecorder.RecordPCMData> mPcmDataList = null;
    private final Object mPcmMutex = new Object();
    private IMSpeexDSP mSpeexDsp;
    private AmrInputStream mAmrInputStream;
    private InputStream mInputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPcmDataList = Collections.synchronizedList(new LinkedList<IMAudioRecorder.RecordPCMData>());
        mAudioTrack = new IMAudioTrack();
        mSpeexDsp = new IMSpeexDSP(mPcmDataList, mPcmMutex);

        mAudioRecorder = new IMAudioRecorder(mPcmDataList, mPcmMutex, mSpeexDsp);

        mInputStream = new InputStream() {
            @Override
            public int read() throws IOException {
                return 0;
            }
        };
        mAmrInputStream = new AmrInputStream(mInputStream);

        findButton();
        setOnClinkListene();
    }

    private void findButton() {
        mStartRecordBtn = (Button)findViewById(R.id.start_record_btn);
        mStopRecordBtn = (Button)findViewById(R.id.stop_record_btn);
        mPlayPcmBtn = (Button)findViewById(R.id.play_pcm_btn);
        mDenoiseAndPlayAmrBtn = (Button)findViewById(R.id.preocess_play_amr_btn);
    }

    Button.OnClickListener mL = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.start_record_btn:
                    new Thread(mAudioRecorder).start();
                    new Thread(mSpeexDsp).start();
                    break;
                case R.id.stop_record_btn:
                    mAudioRecorder.setRecording(false);
                    break;
                case R.id.play_pcm_btn:
                    new Thread(mAudioTrack).start();
                    break;
                case R.id.preocess_play_amr_btn:
                    //new Thread(mSpeexDsp).start();
                    break;
                default:
                    break;
            }
        }
    };

    private void setOnClinkListene() {
        mStartRecordBtn.setOnClickListener(mL);
        mStopRecordBtn.setOnClickListener(mL);
        mPlayPcmBtn.setOnClickListener(mL);
        mDenoiseAndPlayAmrBtn.setOnClickListener(mL);
    }

    /*public void putPCMData(short[] data, int size) {
        IMAudioRecorder.RecordPCMData rd = new IMAudioRecorder.RecordPCMData();
        synchronized (mPcmMutex) {
            rd.size = size;
            System.arraycopy(data, 0, rd.ready, 0, size);
            mPcmDataList.add(rd);
        }
    }*/
    private void startTransfer() {
        new TransferThread(this, new TransferThread.TransferCallback() {

            @Override
            public void onSuccess() {
                transferSuccess();
            }

            @Override
            public void onFailed() {
            }
        }).start();
    }

    private void transferSuccess() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Log.i("shangri", "encode success");
                //waitDialog.dismiss();
                //hintView.setText(getResources().getString(R.string.transfer_result));
                //ToastUtil.showShort(MainActivity.this, R.string.success_hint);
            }
        });
    }
}
