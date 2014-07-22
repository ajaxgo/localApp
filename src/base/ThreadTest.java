package base;

/**
 * @ClassName ThreadTest.java
 * @version 1.0
 * @Description 测试thread类中的sleep方法。sleep睡眠后，只有获得this锁的线程才能进入同步块
 * @author tsw
 * @date 2012-1-11 上午10:18:09
 * @Copyright
 */

public class ThreadTest {

    private String mutex = "mutex";

    public void f (String flag) {
        System.out.println(flag + ", entry mehtod f");
        synchronized (this) {
            System.err.println(flag + ", invoke method f....");
            try {
                Thread.sleep(10 * 1000);
                // wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void g () {
        System.out.println("entey method g ");
        synchronized (this) {
            System.err.println("invoke method g.....");
            notifyAll();
        }
    }

    public static void main (String[] args) {
        // t1
        ThreadTest t1 = new ThreadTest();
        MyThread thread1 = new MyThread();
        MyThread thread2 = new MyThread();
        thread1.test = t1;
        thread2.test = t1;

        // t2
        ThreadTest t2 = new ThreadTest();
        MyThread thread3 = new MyThread();
        MyThread thread4 = new MyThread();
        thread3.test = t2;
        thread4.test = t2;

        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();
    }
}

class MyThread extends Thread {

    ThreadTest test;

    @Override
    public void run () {
        while (true) {
            this.test.f(this.toString());
            // this.test.g();
        }
    }

}
