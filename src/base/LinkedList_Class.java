package base;

import java.util.LinkedList;

/**
 * @ClassName LinkedList_Class.java
 * @version 1.0
 * @Description 该类中的方法涉及到linklist的操作
 * @author tsw
 * @date 2011-12-26 下午10:02:16
 * @Copyright
 */

public class LinkedList_Class {

    /**
     * 5个人围成一圈，每次数4个人，将第四个人剔除出圈，问最后一个人是谁。
     */
    public void findLastOne () {
        Node rear = new Node("five");
        Node one = new Node("one");
        Node two = new Node("two");
        Node three = new Node("three");
        Node four = new Node("four");
        rear.next = one;
        rear.last = four;
        one.last = rear;
        one.next = two;
        two.last = one;
        two.next = three;
        three.last = two;
        three.next = four;
        four.last = three;
        four.next = rear;

        Node head = rear.next;
        while (head != head.last) {
            for (int i = 0; i < 3; i++) {
                head = head.next;
            }
            Node tmp = head.next;
            head.next.last = head.last;
            head.last.next = head.next;
//            head.last = head.next = null;
            System.out.println(head.name);
//            head.name = null;
            head = tmp;
        }

    }

    public static void main (String[] args) {
        LinkedList_Class listObj = new LinkedList_Class();
        listObj.findLastOne();
    }
}

class Node {

    String name;
    Node next;
    Node last;

    public Node (String name) {
        this.name = name;
    }
}
