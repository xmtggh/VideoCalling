#include "example_sszpf_x264_JNI.h"
#include "x264encoder.h"
#include <string.h>
#include <stdlib.h>

#ifndef NELEM
# define NELEM(x) ((int) (sizeof(x) / sizeof((x)[0])))
#endif
#define YUVBUFFER_IN_JAVA_OBJ_NAME   "mVideobuffer"

static JavaVM * VM;
jfieldID fid_yuvbuffer;


struct JavaEnv {
	JavaEnv() {
		if (VM != NULL && VM->AttachCurrentThread(&env, NULL) == JNI_OK) {
			//LOGI("env atarch");
			istarch = true;
		} else {
			//LOGI("env not atarch");
			istarch = false;
		}
	}
	~JavaEnv() {
		if (istarch) {
			//LOGI("env detarch");
			//VM->DetachCurrentThread();
		}
	}
	JNIEnv *env;
	bool istarch;
};
struct JavaMethodID {
	const char* name;
	const char* signature;
	jmethodID id;
	jmethodID getMID(JNIEnv *env, jclass clazz) {
		if (id == NULL) {
			//LOGI("getMID,name=%s,sig=%s,class=%p",name,signature,clazz);
			id = env->GetMethodID(clazz, name, signature);
			//LOGI("getMID,id=%d",id);
		}
		return id;
	}
};

JavaMethodID h264datacallback;
jobject ehobj;
jclass jclz;
//************************************************************************************************************************************/
//************************************************************JNI_METHOD**************************************************************/
//************************************************************************************************************************************/

void CALLBACK H264DataCallBackFunc(void* pdata,int datalen)
{
	LOGI("/**********************H264DataCallBackFunc************************%d",datalen);

	h264datacallback.name = "H264DataCallBackFunc";
	h264datacallback.signature = "([BI)V";
	JavaEnv java;
	if (java.istarch) {
		JNIEnv* menv= NULL;
		VM->AttachCurrentThread(&menv, NULL);
		jbyteArray pcmdata = menv->NewByteArray(datalen);
		menv->SetByteArrayRegion(pcmdata, 0, datalen,(jbyte*)pdata);
		java.env->CallVoidMethod(ehobj,h264datacallback.getMID(java.env, jclz),pcmdata,datalen);
	}
}

static void Java_example_sszpf_x264_initX264Encode(JNIEnv *env, jobject jobj,jint width, jint height, jint fps, jint bite)
{
	env->GetJavaVM(&VM);
	if (VM != NULL && VM->AttachCurrentThread(&env, NULL) == JNI_OK)
	{
 	     ehobj = env->NewGlobalRef(jobj);
		 jclz = (jclass) env->NewGlobalRef(env->GetObjectClass(jobj));
		 fid_yuvbuffer = env->GetFieldID(jclz, YUVBUFFER_IN_JAVA_OBJ_NAME,
			"Ljava/nio/ByteBuffer;");
	}

	initX264Encode(width,height,fps,bite,H264DataCallBackFunc);
}

static void Java_example_sszpf_x264_encoderH264(JNIEnv *env, jobject jobj,jint lenght, jlong time)
{
	jobject input = (jobject) env->GetObjectField(jobj, fid_yuvbuffer);
	unsigned char * jb = (unsigned char *) env->GetDirectBufferAddress(input);
	encoderH264(jb, lenght, time);
}

static void Java_example_sszpf_x264_releaseX264Encode(JNIEnv *env, jobject jobj)
{
	releaseX264Encode();
}

//************************************************************************************************************************************/
//************************************************************JNI_LOAL****************************************************************/
//************************************************************************************************************************************/

static JNINativeMethod gMethods[] = {{"initX264Encode", "(IIII)V",(void *)Java_example_sszpf_x264_initX264Encode},
                                     {"encoderH264", "(IJ)I",(void *)Java_example_sszpf_x264_encoderH264},
                                     {"CloseX264Encode","()V",(void *)Java_example_sszpf_x264_releaseX264Encode}};


int register_Native_Methods(JNIEnv *env) {
	jclass clazz = env->FindClass(kClassPathName);
	if ((env->RegisterNatives(clazz, gMethods, NELEM(gMethods) )) > 1) {
		return -1;
	}
	return 0;
}


jint JNI_OnLoad(JavaVM* vm, void* reserved) {
	JNIEnv* env= NULL;
	jint result = -1;

	if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
		//LOG_PRINT("ERROR: GetEnv failed\n");
		goto bail;
	}

	if (register_Native_Methods(env) < 0) {
		goto bail;
	}
	/* success -- return valid version number */
	result = JNI_VERSION_1_4;

	VM = vm;

	bail: return result;
}
