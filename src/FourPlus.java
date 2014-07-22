import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @ClassName FourPlus.java
 * @version 1.0
 * @Description
 * @author tsw
 * @date 2011-12-15 上午10:19:30
 * @Copyright
 */

public class FourPlus {

    /**
     * @param args
     */
    public static void main (String[] args) {
        List<Integer> list = new ArrayList<Integer>();
        Random ram = new Random();
        for (int i = 0; i < 20; i++) {
            list.add(ram.nextInt(100));
        }
        FourPlus obj = new FourPlus();
        obj.equalN(list, 250);
    }

    /**
     * 任意4个数字之和为N的所有组合
     * 
     * @param list
     * @param N
     */
    public void equalN (List<Integer> list, Integer N) {
        for (int index1 = 0; index1 < list.size(); index1++) {
            for (int index2 = index1 + 1; index2 < list.size(); index2++) {
                for (int index3 = index2 + 1; index3 < list.size(); index3++) {
                    for (int index4 = index3 + 1; index4 < list.size(); index4++) {
                        int count = list.get(index1) + list.get(index2) + list.get(index3) + list.get(index4);
                        if (count == N) {
                            System.out.print(list.get(index1) + "+");
                            System.out.print(list.get(index2) + "+");
                            System.out.print(list.get(index3) + "+");
                            System.out.print(list.get(index4));
                            System.out.println();
                        }
                    }
                }
            }
        }
    }
}
