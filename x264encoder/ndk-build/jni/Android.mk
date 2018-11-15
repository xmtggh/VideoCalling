
LOCAL_PATH := $(call my-dir)

#编译配置

include $(CLEAR_VARS) 
LOCAL_MODULE    := add
LOCAL_SRC_FILES := libx264.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS) 
LOCAL_MODULE := x264encoder
#头文件路径
INCLUDE_PATH = $(LOCAL_PATH)/../common \
#要编译的源文件
SOURCE := $(wildcard $(LOCAL_PATH)/../common/*.cpp) \
		  $(wildcard $(LOCAL_PATH)/../common/*.c) \
		  

SOURCE := $(patsubst jni%,../jni%,$(SOURCE))

LOCAL_C_INCLUDES := $(INCLUDE_PATH)

LOCAL_SRC_FILES := $(SOURCE)

LOCAL_STATIC_LIBRARIES += add 

LOCAL_LDLIBS += -L$(SYSROOT)/usr/lib -llog

include $(BUILD_SHARED_LIBRARY)