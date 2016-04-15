LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE:= libspeexdsp

LOCAL_CFLAGS = -DHAVE_CONFIG_H

LOCAL_C_INCLUDES := $(LOCAL_PATH)/include
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include/libspeexdsp

BUILD_SMALLFT:= true

ifdef BUILD_KISS_FFT
  LOCAL_SRC_FILES += $(LOCAL_PATH)/libspeexdsp/kiss_fft.c \
  libspeexdsp/kiss_fftr.c \
  
  LOCAL_C_INCLUDES += $(LOCAL_PATH)/libspeexdsp/_kiss_fft_guts.h \
  $(LOCAL_PATH)/libspeexdsp/kiss_fft.h \
  $(LOCAL_PATH)/libspeexdsp/kiss_fftr.h
else

ifdef BUILD_SMALLFT
  LOCAL_SRC_FILES += $(LOCAL_PATH)/libspeexdsp/smallft.c
else
endif

endif

LOCAL_SRC_FILES +=\
  $(LOCAL_PATH)/libspeexdsp/preprocess.c \
  $(LOCAL_PATH)/libspeexdsp/jitter.c \
  $(LOCAL_PATH)/libspeexdsp/mdf.c \
  $(LOCAL_PATH)/libspeexdsp/fftwrap.c \
  $(LOCAL_PATH)/libspeexdsp/filterbank.c \
  $(LOCAL_PATH)/libspeexdsp/resample.c \
  $(LOCAL_PATH)/libspeexdsp/buffer.c \
  $(LOCAL_PATH)/libspeexdsp/scal.c \
  $(LOCAL_PATH)/speex_jni.cpp

LOCAL_C_INCLUDES += $(LOCAL_PATH)/../amr_nb \
	$(LOCAL_PATH)/../lib

LOCAL_SHARED_LIBRARIES := libamr-codec

LOCAL_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)