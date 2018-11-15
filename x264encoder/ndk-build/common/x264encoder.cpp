#include "x264encoder.h"
#include "PUX264Encoder.h"

x264Encode *_x264Encoder;
H264DataCallBack h264callbackFunc;

void initX264Encode(int width, int height, int fps, int bite,H264DataCallBack h264callback)
{
	if (NULL != _x264Encoder ) {
        releaseX264Encode();
    }
    _x264Encoder = new x264Encode();
    _x264Encoder->initX264Encode(width, height, fps, bite);
	h264callbackFunc = h264callback;
	LOGI("/**********************initX264Encode*************************/");
}

void encoderH264(void* pdata,unsigned int datalen,long long time)
{
	if(_x264Encoder == NULL)
	{
	   LOGI("_x264Encoder is Null");
	   return;
	}
	int i = 0;
    char *bufdata = NULL;
    int buflen = -1;
    int isKeyFrame;
	//LOGI("/**********************PostOriginalSlice************************%d",datalen);
	_x264Encoder->startEncoder((uint8_t*)pdata, *&bufdata, *&buflen, *&isKeyFrame);
	if(buflen != -1)
	{
		if(NULL != h264callbackFunc)
		{
			h264callbackFunc(bufdata,buflen);
		}
		if(bufdata)
		{
			delete [] bufdata;
		}
	}
}

void releaseX264Encode()
{
	if (_x264Encoder) {
        _x264Encoder->releaseEncoder();
        delete _x264Encoder;
		_x264Encoder = NULL;
    }
}