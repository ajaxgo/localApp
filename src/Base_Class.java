import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

/**
 * @Description:
 * @author: tsw
 * @version: 1.0 2011-9-27
 * @since: JDK1.6
 * @copyright:
 */
public class Base_Class {

    /**
     * 冒泡排序
     * 
     * @param args
     */
    public static void main (String[] args) {

        Base_Class base_a = new Base_Class();
        base_a.sortPop();
        base_a.testList();
        base_a.testReference();

    }

    public void sortPop () {
        Random ran = new Random();
        int[] arr1 = new int[10];
        for (int i = 0; i < 10; i++) {
            arr1[i] = ran.nextInt(100);
        }

        // Arrays.sort(arr1);
        for (int i = 0; i < arr1.length; i++) {
            for (int j = arr1.length - 1; j > i; j--) {
                if (arr1[j - 1] > arr1[j]) {
                    int tmp = arr1[j];
                    arr1[j] = arr1[j - 1];
                    arr1[j - 1] = tmp;
                }
            }
        }
        for (int a : arr1) {
            System.out.println(a);
        }
    }

    /**
     * 测试list使用
     */
    public void testList () {
        List list1 = new ArrayList();
        list1.add(1);
        list1.add(2);
        list1.add(3);
        ListIterator iter = list1.listIterator(2);
        System.out.println(iter.previous());
        System.out.println(iter.previous());
        System.out.println(iter.next());

    }

    public void testReference () {
        Inner obj1 = new Inner();
        obj1.name = "obj1";
        obj1.id = 1;
        System.out.println(obj1.name);
    }

    class Inner {

        String name;
        Integer id;
        Inner next;
    }
}
