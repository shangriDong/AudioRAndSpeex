LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

PV_TOP := $(LOCAL_PATH)/amr_nb/opencore
PV_INCLUDES := $(LOCAL_PATH)/oscl \
					$(PV_TOP)/codecs_v2/audio/gsm_amr/amr_nb/common/include \
					$(PV_TOP)/codecs_v2/audio/gsm_amr/amr_nb/common/src \
					$(PV_TOP)/codecs_v2/audio/gsm_amr/amr_nb/dec/include \
					$(PV_TOP)/codecs_v2/audio/gsm_amr/amr_nb/dec/src \
					$(PV_TOP)/codecs_v2/audio/gsm_amr/amr_nb/enc/include \
					$(PV_TOP)/codecs_v2/audio/gsm_amr/amr_nb/enc/src \
					$(PV_TOP)/codecs_v2/audio/gsm_amr/common/dec/include

include $(PV_TOP)/codecs_v2/audio/gsm_amr/amr_nb/common/Android.mk
include $(PV_TOP)/codecs_v2/audio/gsm_amr/amr_nb/dec/Android.mk
include $(PV_TOP)/codecs_v2/audio/gsm_amr/amr_nb/enc/Android.mk
include $(PV_TOP)/../../amr_nb/Android.mk
include $(PV_TOP)/../../speexdsp/Android.mk

include $(call all-subdir-makefiles)