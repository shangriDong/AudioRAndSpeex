#ifndef AMR_ENC
#define AMR_ENC
#ifdef __cplusplus
extern "C"{
#endif
int amr_enc_init(const char *outfile, int bitsPerSample, int channels, int sampleRate);

int handleAmrEnc(const char *outfile, int channels, short *inputBuf, int size);

int arm_enc_exit();
#ifdef __cplusplus
}
#endif
#endif