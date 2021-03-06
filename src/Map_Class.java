import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/*
 */

/**
 * @Description:
 * @author: tsw
 * @version: 1.0 2011-10-9
 * @since: JDK1.6
 * @copyright:
 */

public class Map_Class {

    /**
     * @param args
     */
    public static void printAll (Iterator iter) {
        while (iter.hasNext())
            System.out.println(iter.next().toString());
    }

    public static void main (String[] args) {

        HashMap<Integer, Integer> this_map = new HashMap<Integer, Integer>();
        Random ran = new Random();
        for (int i = 1; i < 10; i++) {
            this_map.put(i, ran.nextInt(100));
        }
        List<Integer> list = new ArrayList<Integer>();
        for (int i = 1; i < 10; i++) {
            list.add(ran.nextInt());
        }
        Map<String, List> a = new HashMap<String, List>();
        List k = new ArrayList();
        k.add(3);
        a.put("1", k);
        a.get("1").add(1);
        printAll(this_map.keySet().iterator());
        printAll(list.iterator());
        System.out.println("-------------------");
        printAll(a.entrySet().iterator());
    }

}
