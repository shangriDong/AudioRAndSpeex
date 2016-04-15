LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE := amr-codec

LOCAL_SRC_FILES := $(LOCAL_PATH)/amr-enc.c \
				$(LOCAL_PATH)/wrapper.cpp

LOCAL_C_INCLUDES := $(PV_INCLUDES)


LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog

LOCAL_SHARED_LIBRARIES := libpv_amr_nb_common_lib \
						libpvencoder_gsmamr \
						libpvdecoder_gsmamr


include $(BUILD_SHARED_LIBRARY)
