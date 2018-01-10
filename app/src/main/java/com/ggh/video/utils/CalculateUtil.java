package com.ggh.video.utils;

/**
 * Created by ZQZN on 2018/1/9.
 */

public class CalculateUtil {

    /**
     * 注释：int到字节数组的转换！
     *
     * @param number
     * @return
     */
    public static byte[] intToByte(int number) {
        int temp = number;
        byte[] b = new byte[4];
        for (int i = 0; i < b.length; i++) {
            b[i] = new Integer(temp & 0xff).byteValue();// 将最低位保存在最低位
            temp = temp >> 8; // 向右移8位
        }
        return b;
    }


    public static int byteToInt(byte b) {
        //Java 总是把 byte 当做有符处理；我们可以通过将其和 0xFF 进行二进制与得到它的无符值
        return b & 0xFF;
    }


    //byte 数组与 int 的相互转换
    public static int byteArrayToInt(byte[] b) {
        return b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }

    public static byte[] intToByteArray(int a) {
        return new byte[] {
                (byte) ((a >> 24) & 0xFF),
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) (a & 0xFF)
        };
    }


    // 清空buf的值
    public static void memset(byte[] buf, int value, int size) {
        for (int i = 0; i < size; i++) {
            buf[i] = (byte) value;
        }
    }

   /* public static void dump(NALU_t n) {
        System.out.println("len: " + n.len + " nal_unit_type:" + n.nal_unit_type);

    }*/

    // 判断是否为0x000001,如果是返回1
    public static int FindStartCode2(byte[] Buf, int off) {
        if (Buf[0 + off] != 0 || Buf[1 + off] != 0 || Buf[2 + off] != 1)
            return 0;
        else
            return 1;
    }

    // 判断是否为0x00000001,如果是返回1
    public static int FindStartCode3(byte[] Buf, int off) {
        if (Buf[0 + off] != 0 || Buf[1 + off] != 0 || Buf[2 + off] != 0 || Buf[3 + off] != 1)
            return 0;
        else
            return 1;
    }

}