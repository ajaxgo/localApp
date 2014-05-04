import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

/**
 * @Description:
 * @author: tsw
 * @version: 1.0 2011-10-10
 * @since: JDK1.6
 * @copyright:
 */

public class Array_Class {

    /**
     * @param args
     */
    public static void main (String[] args) {

        Array_Class testObj = new Array_Class();
        // testObj.testArrayRef();
        testObj.listAdd(new per("1"), new per("2"), new per("3"));
        testObj.listAdd(1, 2, 3);

    }

    public void testListIterator () {
        String[] array = { "a", "b" };
        List<String> list = new ArrayList<String>(Arrays.asList(array));
        list.add("c");
        System.out.println(list.get(2));
        ListIterator<String> it = list.listIterator();
        it.next();
        it.set("ddd");
        System.out.println(list.get(0) + "---" + list.get(1));
    }

    public void testArrayRef () {
        List<StringBuffer> localList = new ArrayList<StringBuffer>();
        localList.add(new StringBuffer("a"));
        localList.add(new StringBuffer("b"));
        StringBuffer otherRef = localList.get(0);
        otherRef.append("ss");
        System.out.println(localList.get(0));

        List<per> perList = new ArrayList<per>();
        perList.add(new per("1"));
        perList.add(new per("2"));
        per tmp = perList.get(0);
        tmp.a = "4";
        System.out.println(perList.get(0).a);

        List<String> stringList = new ArrayList<String>();
        stringList.add("a");
        stringList.add("b");
        String tempString = stringList.get(0);
        tempString = "c";
        System.out.println(stringList.get(0));

        List<Integer> intgerList = new ArrayList<Integer>();
        intgerList.add(1);
        intgerList.add(2);
        Integer tmpInteger = intgerList.get(0);
        tmpInteger = 4;
        System.out.println(intgerList.get(0));
    }

    public <T> void listAdd (T t1, T t2, T t3) {
        List<T> list = new ArrayList<T>();
        list.add(t1);
        list.add(t2);
        System.out.println(list.get(0));
        T tmp = list.get(0);
        tmp = t3;
        System.out.println(list.get(0));
    }
}

class per {

    public String a;

    public per (String tmp) {
        a = tmp;
    }
}
