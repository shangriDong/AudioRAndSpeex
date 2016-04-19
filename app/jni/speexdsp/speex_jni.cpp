#include <jni.h>

#include <string.h>
#include <stdio.h>
#include <unistd.h>

#include <speex/speex_echo.h>
#include <speex/speex_preprocess.h>

#include <android/log.h>

#include "../amr_nb/amr-enc.h"

#define TAG    "speex_jni" // 这个是自定义的LOG的标识
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__) // 定义LOGD类型  

static int codec_open = 0;

SpeexPreprocessState *denoise_state = NULL;

static JavaVM *gJavaVM;

static int frame_size = 160;
static int sampling_rate = 8000;
static int channel_num = 1;
static int count = 0;
static char *filepath = "/sdcard/amrQAQ.amr";
static char bitsPerSample = 16;

extern "C"
JNIEXPORT jint JNICALL Java_com_ccut_shangri_audiorecorder_IMSpeexDSPAndEnc_init (JNIEnv *env, jobject obj,
                                                                                  jint size, jint rate, jint channel)
{
	if (codec_open++ != 0)
    {
        LOGD("IMSpeexDSPAndEnc jni init fail, it is already init!");
        return -1;
    }

    LOGD("IMSpeexDSPAndEnc jni init");

	frame_size = size;
	sampling_rate = rate;
    channel_num = channel;

    if (NULL == filepath)
    {
        LOGD("IMSpeexDSPAndEnc jni init fail, file path is null!");
        return -1;
    }

	denoise_state = speex_preprocess_state_init(frame_size, sampling_rate);

	int i = 1;
    speex_preprocess_ctl(denoise_state, SPEEX_PREPROCESS_SET_DENOISE, &i);
    i = -25;
    speex_preprocess_ctl(denoise_state, SPEEX_PREPROCESS_SET_NOISE_SUPPRESS, &i);
    i = 0;
    speex_preprocess_ctl(denoise_state, SPEEX_PREPROCESS_SET_AGC, &i);
    i = 0;
    speex_preprocess_ctl(denoise_state, SPEEX_PREPROCESS_SET_DEREVERB, &i);
    float f = 0.0;
    speex_preprocess_ctl(denoise_state, SPEEX_PREPROCESS_SET_DEREVERB_DECAY, &f);
    f = 0.0;
    speex_preprocess_ctl(denoise_state, SPEEX_PREPROCESS_SET_DEREVERB_LEVEL, &f);

    LOGD("IMSpeexDSPAndEnc jni  success!");
	
	//init amr_enc
	amr_enc_init("/sdcard/amrQAQ.amr", 16, 1, 8000);

    count = 0;
}
extern "C"
JNIEXPORT jint JNICALL Java_com_ccut_shangri_audiorecorder_IMSpeexDSPAndEnc_close (JNIEnv *env, jobject obj, jint size)
{
	if (!codec_open)
        return -1;
	speex_preprocess_state_destroy(denoise_state);
	
	//exit amr_enc
    arm_enc_exit();

	codec_open = 0;
}
extern "C"
JNIEXPORT jint JNICALL Java_com_ccut_shangri_audiorecorder_IMSpeexDSPAndEnc_denoiseAndEnc (JNIEnv *env, jobject obj,
        jshortArray lin, jint offset, jint size)
{
    LOGD("IMSpeexDSPAndEnc jni start");

	if (!codec_open)
        return -1;

	short *buffer=(short *)malloc(sizeof(short) * size);
	
	env->GetShortArrayRegion(lin, offset, size, buffer);
    int i = 0;
	for (i = 0; i < size/frame_size; i++)
	{
		//降噪增益处理
		if (speex_preprocess_run(denoise_state, (buffer + i * frame_size)) == 0)
		{
			LOGD("IMSpeexDSP_denoise 静音或噪音！");
		}

		handleAmrEnc("/sdcard/amrQAQ.amr", 1, buffer + i * frame_size, frame_size);
        ++count;
	}
	free(buffer);
    LOGD("count = %d", count);
    LOGD("IMSpeexDSPAndEnc jni end");
}