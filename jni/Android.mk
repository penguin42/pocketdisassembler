LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_CFLAGS := -I$(LOCAL_PATH)/../binutils/result/include
LOCAL_LDLIBS := -L$(LOCAL_PATH)/../binutils/result/lib -lopcodes -lbfd
LOCAL_MODULE := binutilsglue
LOCAL_SRC_FILES := binutilsglue.c

include $(BUILD_SHARED_LIBRARY)
