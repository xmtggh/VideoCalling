#include <stdio.h>

extern "C" {
   #include "x264.h"
   #include "x264_config.h"
}

enum bitrate_lev
{
    BIT_HIGH_LEVEL = 0,
    BIT_STANDARD_LEVEL = 1,
    BIT_MEDIUM_LEVEL = 2,
    BIT_LOW_LEVEL = 3,
};


class x264Encode{
    
    public :
        x264_param_t *_x264_param;
        x264_t       *_x264_encoder;
        x264_picture_t *_in_pic;
    
        x264_picture_t *_out_pic;
    
        uint8_t * picture_buf;
    
        unsigned int bitratelevel;
    private :
        int xwidth;
        int xheight;
        int xfps;
    
    public :
        void initX264Encode(int width, int height, int fps, int bite);
        void startEncoder(uint8_t * dataptr,char *&bufdata,int &buflen, int &isKeyFrame);
        void releaseEncoder();

    private :
        void Flush();

    
};