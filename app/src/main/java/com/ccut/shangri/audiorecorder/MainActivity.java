package com.ccut.shangri.audiorecorder;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;

public class MainActivity extends Activity {

    private Button mStartRecordBtn;
    private Button mPlayPcmBtn;
    private Button mDenoiseAndPlayAmrBtn;
    private Button mStopRecordBtn;
    private IMAudioRecorder mAudioRecorder;
    private IMAudioTrack mAudioTrackPCM;
    private IMAudioTrack mAudioTrackAMR;
    private Button mPlayDenoisePCMBtn;
    private Button mPlayDenoiseAmrBtn;
    private Button mZhiyaAmrBtn;

    private IMSpeexDSP mIMSpeexDSP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAudioTrackPCM = new IMAudioTrack("/reverseme.pcm");
        mAudioTrackAMR = new IMAudioTrack("/adcd.pcm");

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
        mPlayDenoisePCMBtn = (Button)findViewById(R.id.play_amr_btn);
        mPlayDenoiseAmrBtn = (Button)findViewById(R.id.play_denose_amr_btn);
        mZhiyaAmrBtn = (Button)findViewById(R.id.amr_btn);
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
                    Log.d("shangri", "play pcm /adcd.pcm");
                    new Thread(mAudioTrackAMR).start();
                    break;
                case R.id.play_denose_amr_btn:
                    MediaPlayer mp=new MediaPlayer();
                    try {
                        mp.setDataSource(Environment.getExternalStorageDirectory().getAbsolutePath() + "/denoise_amr.amr");
                        mp.prepare();
                        mp.start();
                    } catch (IOException e) {
                        Log.e("shangri", "" +e);
                    }
                    break;
                case R.id.amr_btn:
                    startTransfer();
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
        mPlayDenoisePCMBtn.setOnClickListener(mL);
        mPlayDenoiseAmrBtn.setOnClickListener(mL);
        mZhiyaAmrBtn.setOnClickListener(mL);
    }

    private void startTransfer() {
        new TransferThread(this, new TransferThread.TransferCallback() {

            @Override
            public void onSuccess() {
                Log.i("shangri", "play testZhiya");
                //transferSuccess();
                MediaPlayer mp=new MediaPlayer();
                try {
                    mp.setDataSource(Environment.getExternalStorageDirectory().getAbsolutePath() + "/testZhiya.amr");
                    mp.prepare();
                    mp.start();
                } catch (IOException e) {
                    Log.e("shangri", "" +e);
                }
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
