package com.ccut.shangri.audiorecorder;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

    private Button mStartRecordBtn;
    private Button mPlayPcmBtn;
    private Button mDenoiseAndPlayAmrBtn;
    private Button mStopRecordBtn;
    private IMAudioRecorder mAudioRecorder;
    private IMAudioTrack mAudioTrackPCM;
    private IMAudioTrack mAudioTrackAMR;
    private Button mPlayAmrBtn;

    private IMSpeexDSP mIMSpeexDSP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAudioTrackPCM = new IMAudioTrack("/reverseme.pcm");
        mAudioTrackAMR = new IMAudioTrack("/denose.pcm");

        mAudioRecorder = new IMAudioRecorder();
        mIMSpeexDSP = new IMSpeexDSP();
        findButton();
        setOnClinkListene();
    }

    private void findButton() {
        mStartRecordBtn = (Button)findViewById(R.id.start_record_btn);
        mStopRecordBtn = (Button)findViewById(R.id.stop_record_btn);
        mPlayPcmBtn = (Button)findViewById(R.id.play_pcm_btn);
        mDenoiseAndPlayAmrBtn = (Button)findViewById(R.id.preocess_play_amr_btn);
        mPlayAmrBtn = (Button)findViewById(R.id.play_amr_btn);
    }

    Button.OnClickListener mL = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.start_record_btn:
                    new Thread(mAudioRecorder).start();
                    break;
                case R.id.stop_record_btn:
                    mAudioRecorder.setRecording(false);
                    break;
                case R.id.play_pcm_btn:
                    new Thread(mAudioTrackPCM).start();
                    break;
                case R.id.preocess_play_amr_btn:
                    Log.d("shangri", "preocess_play_amr_btn start");
                    new Thread(new SpeexThread()).start();
                    Log.d("shangri", "preocess_play_amr_btn end");
                    break;
                case R.id.play_amr_btn:
                    new Thread(mAudioTrackAMR).start();
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
        mPlayAmrBtn.setOnClickListener(mL);
    }

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

    class SpeexThread implements Runnable {
        @Override
        public void run() {
            IMSpeexDSP.file();
        }
    }
}
