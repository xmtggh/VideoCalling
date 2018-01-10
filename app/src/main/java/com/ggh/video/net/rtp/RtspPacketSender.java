package com.ggh.video.net.rtp;

import com.ggh.video.utils.CalculateUtil;

/**
 * Created by ZQZN on 2018/1/9.
 */

public class RtspPacketSender {

    private static final String TAG = "RtspPacketSender";


    //------------视频转换数据监听-----------
    public interface H264ToRtpLinsener {
        void h264ToRtpResponse(byte[] out, int len);
    }

    private H264ToRtpLinsener h264ToRtpLinsener;

    //执行回调
    private void exceuteH264ToRtpLinsener(byte[] out, int len) {
        if (this.h264ToRtpLinsener != null) {
            h264ToRtpLinsener.h264ToRtpResponse(out, len);
        }
    }


    // -------视频--------
    private int framerate = 25;
    private byte[] sendbuf = new byte[1500];
    private int packageSize = 1400;
    private int seq_num = 0;
    private int timestamp_increse = (int) (90000.0 / framerate);//framerate是帧率
    private int ts_current = 0;
    private int bytes = 0;

    // -------视频END--------

    public RtspPacketSender(H264ToRtpLinsener h264ToRtpLinsener) {
        this.h264ToRtpLinsener = h264ToRtpLinsener;
    }



    /**
     * 一帧一帧的RTP封包
     *
     * @param r
     * @return
     */
    public void h264ToRtp(byte[] r, int h264len) throws Exception {

        CalculateUtil.memset(sendbuf, 0, 1500);
        // 负载类型号96,其值为：01100000
        sendbuf[1] = (byte) (sendbuf[1] | 96);
        // 版本号,此版本固定为2
        sendbuf[0] = (byte) (sendbuf[0] | 0x80);
        //标志位，由具体协议规定其值，其值为：01100000
        sendbuf[1] = (byte) (sendbuf[1] & 254);
        //随机指定10，并在本RTP回话中全局唯一,java默认采用网络字节序号 不用转换（同源标识符的最后一个字节）
        sendbuf[11] = 10;
        if (h264len <= packageSize) {
            // 设置rtp M位为1，其值为：11100000，分包的最后一片，M位（第一位）为0，后7位是十进制的96，表示负载类型
            sendbuf[1] = (byte) (sendbuf[1] | 0x80);
            sendbuf[3] = (byte) seq_num++;
            //send[2]和send[3]为序列号，共两位
            System.arraycopy(CalculateUtil.intToByte(seq_num++), 0, sendbuf, 2, 2);
            {
                // java默认的网络字节序是大端字节序（无论在什么平台上），因为windows为小字节序，所以必须倒序
                /**参考：
                 * http://blog.csdn.net/u011068702/article/details/51857557
                 * http://cpjsjxy.iteye.com/blog/1591261
                 */
                byte temp = 0;
                temp = sendbuf[3];
                sendbuf[3] = sendbuf[2];
                sendbuf[2] = temp;
            }
            // FU-A HEADER, 并将这个HEADER填入sendbuf[12]
            sendbuf[12] = (byte) (sendbuf[12] | ((byte) (r[0] & 0x80)) << 7);
            sendbuf[12] = (byte) (sendbuf[12] | ((byte) ((r[0] & 0x60) >> 5)) << 5);
            sendbuf[12] = (byte) (sendbuf[12] | ((byte) (r[0] & 0x1f)));
            // 同理将sendbuf[13]赋给nalu_payload
            //NALU头已经写到sendbuf[12]中，接下来则存放的是NAL的第一个字节之后的数据。所以从r的第二个字节开始复制
            System.arraycopy(r, 1, sendbuf, 13, h264len - 1);
            ts_current = ts_current + timestamp_increse;
            //序列号接下来是时间戳，4个字节，存储后也需要倒序
            System.arraycopy(CalculateUtil.intToByte(ts_current), 0, sendbuf, 4, 4);
            {
                byte temp = 0;
                temp = sendbuf[4];
                sendbuf[4] = sendbuf[7];
                sendbuf[7] = temp;
                temp = sendbuf[5];
                sendbuf[5] = sendbuf[6];
                sendbuf[6] = temp;
            }
            //获sendbuf的长度,为nalu的长度(包含nalu头但取出起始前缀,加上rtp_header固定长度12个字节)
            bytes = h264len + 12;
            //client.send(new DatagramPacket(sendbuf, bytes, addr, port/*9200*/));
            //send(sendbuf,bytes);
            exceuteH264ToRtpLinsener(sendbuf, bytes);

        } else if (h264len > packageSize) {
            int k = 0, l = 0;
            k = h264len / packageSize;
            l = h264len % packageSize;
            int t = 0;
            ts_current = ts_current + timestamp_increse;
            //时间戳，并且倒序
            System.arraycopy(CalculateUtil.intToByte(ts_current), 0, sendbuf, 4, 4);
            {
                byte temp = 0;
                temp = sendbuf[4];
                sendbuf[4] = sendbuf[7];
                sendbuf[7] = temp;
                temp = sendbuf[5];
                sendbuf[5] = sendbuf[6];
                sendbuf[6] = temp;
            }
            while (t <= k) {
                //序列号，并且倒序
                System.arraycopy(CalculateUtil.intToByte(seq_num++), 0, sendbuf, 2, 2);
                {
                    byte temp = 0;
                    temp = sendbuf[3];
                    sendbuf[3] = sendbuf[2];
                    sendbuf[2] = temp;
                }
                if (t == 0) {//分包的第一片
                    //其值为：01100000，不是最后一片，M位（第一位）设为0
                    sendbuf[1] = (byte) (sendbuf[1] & 0x7F);
                    //FU indicator，一个字节，紧接在RTP header之后，包括F,NRI，header
                    //禁止位，为0
                    sendbuf[12] = (byte) (sendbuf[12] | ((byte) (r[0] & 0x80)) << 7);
                    //NRI，表示包的重要性
                    sendbuf[12] = (byte) (sendbuf[12] | ((byte) ((r[0] & 0x60) >> 5)) << 5);
                    sendbuf[12] = (byte) (sendbuf[12] | (byte) (28));//TYPE，表示此FU-A包为什么类型，一般此处为28
                    //FU header，一个字节，S,E，R，TYPE
                    sendbuf[13] = (byte) (sendbuf[13] & 0xBF);//E=0，表示是否为最后一个包，是则为1
                    sendbuf[13] = (byte) (sendbuf[13] & 0xDF);//R=0，保留位，必须设置为0
                    sendbuf[13] = (byte) (sendbuf[13] | 0x80);//S=1，表示是否为第一个包，是则为1
                    sendbuf[13] = (byte) (sendbuf[13] | ((byte) (r[0] & 0x1f)));//TYPE，即NALU头对应的TYPE
                    //将除去NALU头剩下的NALU数据写入sendbuf的第14个字节之后。前14个字节包括：12字节的RTP Header，FU indicator，FU header
                    System.arraycopy(r, 1, sendbuf, 14, packageSize);
                    //client.send(new DatagramPacket(sendbuf, packageSize + 14, addr, port/*9200*/));
                    exceuteH264ToRtpLinsener(sendbuf, packageSize + 14);
                    t++;
                } else if (t == k) {//分片的最后一片
                    sendbuf[1] = (byte) (sendbuf[1] | 0x80);

                    sendbuf[12] = (byte) (sendbuf[12] | ((byte) (r[0] & 0x80)) << 7);
                    sendbuf[12] = (byte) (sendbuf[12] | ((byte) ((r[0] & 0x60) >> 5)) << 5);
                    sendbuf[12] = (byte) (sendbuf[12] | (byte) (28));

                    sendbuf[13] = (byte) (sendbuf[13] & 0xDF); //R=0，保留位必须设为0
                    sendbuf[13] = (byte) (sendbuf[13] & 0x7F); //S=0，不是第一个包
                    sendbuf[13] = (byte) (sendbuf[13] | 0x40); //E=1，是最后一个包
                    sendbuf[13] = (byte) (sendbuf[13] | ((byte) (r[0] & 0x1f)));//NALU头对应的type

                    if (0 != l) {//如果不能整除，则有剩下的包，执行此代码。如果包大小恰好是1400的倍数，不执行此代码。
                        System.arraycopy(r, t * packageSize + 1, sendbuf, 14, l - 1);//l-1，不包含NALU头
                        bytes = l - 1 + 14; //bytes=l-1+14;
                        //client.send(new DatagramPacket(sendbuf, bytes, addr, port/*9200*/));
                        //send(sendbuf,bytes);
                        exceuteH264ToRtpLinsener(sendbuf, bytes);
                    }//pl
                    t++;
                } else if (t < k && 0 != t) {//既不是第一片，又不是最后一片的包
                    sendbuf[1] = (byte) (sendbuf[1] & 0x7F); //M=0，其值为：01100000，不是最后一片，M位（第一位）设为0.
                    sendbuf[12] = (byte) (sendbuf[12] | ((byte) (r[0] & 0x80)) << 7);
                    sendbuf[12] = (byte) (sendbuf[12] | ((byte) ((r[0] & 0x60) >> 5)) << 5);
                    sendbuf[12] = (byte) (sendbuf[12] | (byte) (28));

                    sendbuf[13] = (byte) (sendbuf[13] & 0xDF); //R=0，保留位必须设为0
                    sendbuf[13] = (byte) (sendbuf[13] & 0x7F); //S=0，不是第一个包
                    sendbuf[13] = (byte) (sendbuf[13] & 0xBF); //E=0，不是最后一个包
                    sendbuf[13] = (byte) (sendbuf[13] | ((byte) (r[0] & 0x1f)));//NALU头对应的type
                    System.arraycopy(r, t * packageSize + 1, sendbuf, 14, packageSize);//不包含NALU头
                    //client.send(new DatagramPacket(sendbuf, packageSize + 14, addr, port/*9200*/));
                    //send(sendbuf,1414);
                    exceuteH264ToRtpLinsener(sendbuf, packageSize + 14);

                    t++;
                }
            }
        }
    }


}