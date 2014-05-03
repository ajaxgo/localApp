/*
 * 项目名称：新一代天气雷达信息共享平台 （CIMISS） 类 名: PalindromicNumber.java 版本信息：V1.0 日 期：2011-6-29 版 权：Copyright 2010 中国气象信息中心
 */

/**
 * @Description:
 * @author: tsw
 * @version: 1.0 2011-6-29
 * @since: JDK1.6
 * @copyright: 中国气象信息中心 2011-6-29
 */
// 3位数乘积得到最大的回文
public class PalindromicNumber {

    /**
     * @param args
     */
    public static void main (String[] args) {
        PalindromicNumber thisObj = new PalindromicNumber();
        thisObj.getMaxPalindromic();
        // int elemA = 0, elemB = 0, result = 0;
        // for (int i = 100; i < 1000; i++) {
        // // elemA = i;
        // for (int j = 100; j < 1000; j++) {
        // // elemB = j;
        // int tmp = i * j;
        // if (thisObj.checkPalindromic(tmp)) {
        // if (tmp > result) {
        // result = tmp;
        // elemA = i;
        // elemB = j;
        // }
        // }
        // }
        // }
        // System.out.println(result);
        // System.out.println(elemA + ":" + elemB);

    }

    public boolean checkPalindromic (int param) {
        StringBuffer strBuffer = new StringBuffer(String.valueOf(param));
        String reverse = strBuffer.reverse().toString();
        if (reverse.equals(String.valueOf(param))) {
            return true;
        } else
            return false;
    }

    public void getMaxPalindromic () {
        for (int i = 1000000; i > 999; i--) {
            if (this.checkPalindromic(i)) {
                for (int j = 9999; j > 100; j--) {
                    if ( (i % j == 0) && (i / j > 100) && (i / j < 999)) {
                        System.out.println(i);
                        return;
                    }
                }
            }
        }
    }

}
