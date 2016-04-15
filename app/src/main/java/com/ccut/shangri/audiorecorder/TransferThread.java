package com.ccut.shangri.audiorecorder;

import android.content.Context;
import android.os.Environment;

import java.io.IOException;
import java.io.InputStream;

public class TransferThread extends Thread{
	
	private TransferCallback callback;
	private Context context;
	public TransferThread(Context context, TransferCallback callback){
		this.callback = callback;
		this.context = context;
	}
	
	@Override
	public void run() {
		transfer();
	}
	
	private void transfer(){
		//AmrEncoder.pcm2Amr(Environment.getExternalStorageDirectory().getAbsolutePath() + "/reverseme.pcm",
				//Environment.getExternalStorageDirectory().getAbsolutePath() + "/reverseme.amr");
		String rootPath = Environment.getExternalStorageDirectory().getPath();
        String amrPath = rootPath + "/testZhiya.amr";
        //try {
            //InputStream pcmStream = context.getAssets().open(Environment.getExternalStorageDirectory().getAbsolutePath() + "/reverseme.pcm");
            AmrEncoder.pcm2Amr(Environment.getExternalStorageDirectory().getAbsolutePath() + "/reverseme.pcm", amrPath);
            callback.onSuccess();
        //} catch (IOException e) {
        	//callback.onFailed();
            //e.printStackTrace();
        //}
	}
	
	
	public static interface TransferCallback{
		
		void onSuccess();
		
		void onFailed();
	}

}
