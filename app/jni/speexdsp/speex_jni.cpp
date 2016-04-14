#include <jni.h>

#include <string.h>
#include <unistd.h>

#include <speex/speex_echo.h>
#include <speex/speex_preprocess.h>

#include <android/log.h>  

#include <fstream>
using namespace std;

#define TAG    "shangri" // 这个是自定义的LOG的标识  
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__) // 定义LOGD类型  

static int codec_open = 0;

SpeexPreprocessState *denoise_state = NULL;

static int frame_size;;

static JavaVM *gJavaVM;

static jbyte output_buffer[10000 * 2];

extern "C"
JNIEXPORT int JNICALL Java_com_ccut_shangri_audiorecorder_IMSpeexDSP_open (JNIEnv *env, jobject obj, jint size)
{
    LOGD("shangri IMSpeexDSP_open");
	if (codec_open++ != 0)
        return -1;

	frame_size = size;
	denoise_state = speex_preprocess_state_init(320, 16000);

	int denoise = 1;
    int noiseSuppress = -10;
    speex_preprocess_ctl(denoise_state, SPEEX_PREPROCESS_SET_DENOISE, &denoise);// 降噪
    speex_preprocess_ctl(denoise_state, SPEEX_PREPROCESS_SET_NOISE_SUPPRESS, &noiseSuppress);// 设置噪音的dB

	/*int agc = 1;
    int level = 24000;
    //actually default is 8000(0,32768),here make it louder for voice is not loudy enough by default.
    speex_preprocess_ctl(denoise_state, SPEEX_PREPROCESS_SET_AGC, &agc);// 增益
    speex_preprocess_ctl(denoise_state, SPEEX_PREPROCESS_SET_AGC_LEVEL, &level);*/
    LOGD("shangri IMSpeexDSP_open success!");
}

extern "C"
JNIEXPORT int JNICALL Java_com_ccut_shangri_audiorecorder_IMSpeexDSP_close (JNIEnv *env, jobject obj, jint size)
{
	if (!codec_open)
        return -1;
	speex_preprocess_state_destroy(denoise_state);
}

extern "C"
JNIEXPORT int JNICALL Java_com_ccut_shangri_audiorecorder_IMSpeexDSP_denoise (JNIEnv *env, jobject obj,
        jshortArray lin, jint offset, jbyteArray encoded, jint size)
{
    LOGD("shangri IMSpeexDSP_denoise start");
	//frame_size = size;

	if (!codec_open)
        return -1;

	short *buffer=(short *)malloc(sizeof(short) * frame_size);


    //env->GetShortArrayRegion(lin, offset, size, buffer);
    //env->GetShortArrayRegion(lin, offset, size, (short *)output_buffer);

    //降噪增益处理
    if (speex_preprocess_run(denoise_state, buffer) == 0)
    {
        LOGD("shangri IMSpeexDSP_denoise 静音或噪音！");
    }

    //env->SetByteArrayRegion (encoded, offset, frame_size, output_buffer);

	free(buffer);
    LOGD("shangri IMSpeexDSP_denoise end");
}

#define NN 160
static int filecode = 0;
static SpeexPreprocessState *st;
extern "C"
JNIEXPORT int JNICALL Java_com_ccut_shangri_audiorecorder_IMSpeexDSP_file (JNIEnv *env, jobject obj,
        jshortArray lin, jint offset, jbyteArray encoded, jint size)
{
    if(filecode++ != 0)
        return -1;

    LOGD("shangri Java_com_ccut_shangri_audiorecorder_IMSpeexDSP_file start");
	FILE *stdin_sF = fopen("/sdcard/reverseme.pcm", "rb");
    FILE *stdout_sF = fopen("/sdcard/denose.pcm", "wb");

	short in[NN];
	int count=0;
	st = speex_preprocess_state_init(NN, 8000);

	if (st == NULL)
	{
		LOGD("shangri st == NULL");
		return -1;
	}
	int i = 1;
    speex_preprocess_ctl(st, SPEEX_PREPROCESS_SET_DENOISE, &i);
    i = -25;
    speex_preprocess_ctl(st, SPEEX_PREPROCESS_SET_NOISE_SUPPRESS, &i);
    i = 0;
    speex_preprocess_ctl(st, SPEEX_PREPROCESS_SET_AGC, &i);
    i = 0;
    speex_preprocess_ctl(st, SPEEX_PREPROCESS_SET_DEREVERB, &i);
    float f = 0.0;
    speex_preprocess_ctl(st, SPEEX_PREPROCESS_SET_DEREVERB_DECAY, &f);
    f = 0.0;
    speex_preprocess_ctl(st, SPEEX_PREPROCESS_SET_DEREVERB_LEVEL, &f);


    while (1)
    {
		int vad;
		int readed = fread(in, sizeof(char), NN*2, stdin_sF);

        if (feof(stdin_sF)) {
            LOGD("feof(stdin_sF), readed = %d", readed);
            break;
        }

		if(readed == NN){
			vad = speex_preprocess_run(st, in);

			//LOGD("vad = %d ", vad);
		}

        readed = fwrite(in, sizeof(char), NN*2, stdout_sF);

		count++;
		LOGD("count = %d write_readed = %d", count, readed);
    }

	speex_preprocess_state_destroy(st);
	st = NULL;
    filecode = 0;
    LOGD("shangri Java_com_ccut_shangri_audiorecorder_IMSpeexDSP_file end");
}