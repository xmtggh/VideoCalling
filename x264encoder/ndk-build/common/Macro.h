#ifdef WIN32
#define dllexport __declspec(dllexport)
#define CALLBACK __stdcall
#else
#define dllexport 
#define CALLBACK
#endif

#ifdef __ANDROID__
#include <android/log.h>
#define LOG_TAG "ehSDK"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__) // 定义LOGD类型
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__) // 定义LOGI类型
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__) // 定义LOGW类型
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__) // 定义LOGE类型
#define LOGF(...) __android_log_print(ANDROID_LOG_FATAL,LOG_TAG,__VA_ARGS__) // 定义LOGF类型
#else
#define LOGD(...) //
#define LOGI(...) //
#define LOGW(...) //
#define LOGE(...) //
#define LOGF(...) //
#endif