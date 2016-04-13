#include <jni.h>

#include <string.h>
#include <unistd.h>

#include <speex/speex_echo.h>
#include <speex/speex_preprocess.h>

#include <android/log.h>  
#include <stdio.h>

#define TAG    "shangri" // 这个是自定义的LOG的标识  
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__) // 定义LOGD类型  

static int codec_open = 0;

SpeexPreprocessState *denoise_state = NULL;

static int frame_size;;

static JavaVM *gJavaVM;

extern "C"
JNIEXPORT int JNICALL Java_com_ccut_shangri_speex_speex_encode_Speex_open (JNIEnv *env, jobject obj, jint size)
{
	if (codec_open++ != 0)
        return -1;
	
	frame_size = size;
	denoise_state = speex_preprocess_state_init(frame_size, 44100);
	
	int denoise = 1;
    int noiseSuppress = -10;
    speex_preprocess_ctl(denoise_state, SPEEX_PREPROCESS_SET_DENOISE, &denoise);// 降噪
    speex_preprocess_ctl(denoise_state, SPEEX_PREPROCESS_SET_NOISE_SUPPRESS, &noiseSuppress);// 设置噪音的dB
	
	int agc = 1;
    int level = 24000;
    //actually default is 8000(0,32768),here make it louder for voice is not loudy enough by default.
    speex_preprocess_ctl(denoise_state, SPEEX_PREPROCESS_SET_AGC, &agc);// 增益
    speex_preprocess_ctl(denoise_state, SPEEX_PREPROCESS_SET_AGC_LEVEL, &level);
}

extern "C"
JNIEXPORT int JNICALL Java_com_ccut_shangri_speex_speex_encode_Speex_close (JNIEnv *env, jobject obj, jint size)
{
	if (!codec_open)
        return -1;
	speex_preprocess_state_destroy(denoise_state);
}

extern "C"
JNIEXPORT int JNICALL Java_com_ccut_shangri_speex_speex_encode_Speex_denoise (JNIEnv *env, jobject obj,
        jshortArray lin, jint offset, jbyteArray encoded, jint size)
{
    //int tmp = 8;
	frame_size = size;
	
	if (!codec_open)
        return -1;

	short *buffer=(short *)malloc(sizeof(short)*frame_size);

    env->GetShortArrayRegion(lin, offset + 1 * frame_size, frame_size, buffer);

    speex_preprocess_run(denoise_state, buffer); //降噪增益处理

	free(buffer);
    
}