package base;

/**
 * @ClassName ThreadWaitTest.java
 * @version 1.0
 * @Description
 * @author tsw
 * @date 2012-1-11 上午11:25:15
 * @Copyright
 */

public class ThreadWaitTest {

    public static void main (String[] args) {

        Mutex mutex = new Mutex();
        mutex.count = 0;
        Produce p = new Produce();
        p.mutex = mutex;
        Consume c = new Consume();
        c.mutex = mutex;
        c.produce = p;
        p.consume = c;
        for (int i = 0; i < 10; i++) {
            new Thread(p).start();
        }
        for (int i = 0; i < 10; i++) {
            new Thread(c).start();
        }

    }
}

class Mutex {

    Integer count;
}

/**
 * @ClassName ThreadWaitTest.java
 * @version 1.0
 * @Description 生产者，当大于5时生产者对象阻塞，大于0时消费者对象notify
 * @author tsw
 * @date 2012-1-12 下午4:56:30
 * @Copyright 
 */

class Produce implements Runnable {

    Mutex mutex;
    Consume consume;

    public void run () {
        System.out.println("produce........" + mutex.count);
        while (true) {
            synchronized (this) {
                if (mutex.count >= 5) {
                    try {
                        System.err.println("produce........" + this.toString() + "............" + mutex.count);
                        this.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (mutex.count < 5) {
                    mutex.count++;
                }

            }
            synchronized (consume) {
                if (mutex.count > 0) {
                    consume.notifyAll();
                }
            }

            System.out.println(Thread.currentThread() + "*******************" + mutex.count);
        }

    }
}

/**
 * @ClassName ThreadWaitTest.java
 * @version 1.0
 * @Description 消费者，小于0时消费者阻塞，小于5时生产者notify
 * @author tsw
 * @date 2012-1-12 下午4:58:05
 * @Copyright 
 */

class Consume implements Runnable {

    Mutex mutex;

    Produce produce;

    public void run () {
        System.out.println("consume........" + mutex.count);
        while (true) {
            synchronized (produce) {
                System.err.println("consume........" + this.toString() + "............" + mutex.count);

                if (mutex.count < 5)
                    produce.notifyAll();

            }
            synchronized (this) {
                if (mutex.count > 0)
                    mutex.count--;
                if (mutex.count <= 0) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }

        }

    }
}
