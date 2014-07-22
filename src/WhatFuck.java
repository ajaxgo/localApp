import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @author: 1
 * @version: 1.0 2011-6-23
 * @since: JDK1.6
 * @copyright: 2011-6-23 18750 910107:476 -----14563 910107:476 ------5078 910107:476
 */

public class WhatFuck {

    /**
     * 求10W以下所有质数
     * 
     * @param args
     */
    private static List bufferArray = new ArrayList();

    public static void main (String[] args) {
        // WhatFuck dd = new WhatFuck();
        int k = 1;
        int oo[] = new int[2];
        oo[0] = 0;
        oo[1] = 0;
        Map dataMap = new HashMap();
        dataMap.put(1, 1);
        Long tmpStart = System.currentTimeMillis();
        // for (int i = 1000000; i >= 2; i--) {
        for (int i = 2; i <= 1000000; i++) {
            // List bufferArray = new ArrayList();

            bufferArray.clear();
            // List rr = dd.tmpList(i);
            // k = dd.tmpList(i).size();
            int n = i;
            // bufferArray.clear();
            bufferArray.add(n);
            while (n > 1) {
                if (n % 2 == 0) {
                    n = n / 2;
                } else {
                    n = n * 3 + 1;
                }
                if (dataMap.containsKey(n)) {
                    k = bufferArray.size() + (Integer) dataMap.get(n);
                    break;
                }
                bufferArray.add(n);

            }
            // k = bufferArray.size();
            dataMap.put(i, k);
            if (k > oo[1]) {
                oo[0] = i;
                oo[1] = k;
            }
            k = 1;
        }
        System.out.println(System.currentTimeMillis() - tmpStart);
        System.out.println(oo[0] + ":" + oo[1]);
    }

    // public static void main (String[] args) {
    //
    // WhatFuck dd = new WhatFuck();
    // int k;
    // int m;
    // int oo[] = new int[2];
    // oo[0] = 0;
    // oo[1] = 0;
    // for (int i = 1000000; i >= 2; i--) {
    //
    // int n = i;
    // List bufferArray = new ArrayList();
    // bufferArray.add(n);
    // while (n > 1) {
    // if (n % 2 == 0) {
    // n = n / 2;
    // } else {
    // n = 3 * n + 1;
    // }
    // bufferArray.add(n);
    // }
    //
    // //
    // // List rr = dd.fuck(i);
    // k = bufferArray.size();
    // if (k > oo[1]) {
    // oo[0] = i;
    // oo[1] = k;
    // }
    // }
    // System.out.println(oo[0] + ":" + oo[1]);
    // }

    // public List tmpList (int n) {
    // // List bufferArray = new ArrayList();
    // bufferArray.clear();
    // bufferArray.add(n);
    // while (n != 1) {
    // if (n % 2 == 0) {
    // n = n / 2;
    // } else {
    // n = n * 3 + 1;
    // }
    // bufferArray.add(n);
    //
    // }
    // return bufferArray;
    // }
}
