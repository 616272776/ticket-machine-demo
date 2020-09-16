package com.example.ticketmachinedemo.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author: 苏敏
 * @date: 2020/9/11 12:05
 */
public class ByteUtils {
    public static byte[] HexString2Byte(String str){
        byte[] destByte = new byte[str.length()/2];
        int j=0;
        for(int i=0;i<destByte.length;i++) {
            byte high = (byte) (Character.digit(str.charAt(j), 16) & 0xff);
            byte low = (byte) (Character.digit(str.charAt(j + 1), 16) & 0xff);
            destByte[i] = (byte) (high << 4 | low);
            j+=2;
        }
        return destByte;
    }

    public static String getDate() {

        Date date = new Date();
        SimpleDateFormat f=new SimpleDateFormat("yyMMddHHmmss");
        String format = f.format(date);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 6; i++) {
            String substring = format.substring(i * 2, i * 2 + 2);
            String s = Integer.toHexString(Integer.parseInt(substring));
            if(s.length()==1){
                s='0'+s;
            }
            sb.append(s);

        }
        return sb.toString();
    }

    private static final char HexCharArr[] = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};

    private static final String HexStr = "0123456789abcdef";

    public static byte[] hexStringToByte(String hex) {
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }
        return result;
    }

    private static byte toByte(char c) {
        byte b = (byte) "0123456789ABCDEF".indexOf(c);
        return b;
    }

    public static String byteArrToHex(byte[] btArr) {
        char strArr[] = new char[btArr.length * 2];
        int i = 0;
        for (byte bt : btArr) {
            strArr[i++] = HexCharArr[bt>>>4 & 0xf];
            strArr[i++] = HexCharArr[bt & 0xf];
        }
        return new String(strArr);
    }

    public static byte[] hexToByteArr(String hexStr) {
        char[] charArr = hexStr.toCharArray();
        byte btArr[] = new byte[charArr.length / 2];
        int index = 0;
        for (int i = 0; i < charArr.length; i++) {
            int highBit = HexStr.indexOf(charArr[i]);
            int lowBit = HexStr.indexOf(charArr[++i]);
            btArr[index] = (byte) (highBit << 4 | lowBit);
            index++;
        }
        return btArr;
    }
}
