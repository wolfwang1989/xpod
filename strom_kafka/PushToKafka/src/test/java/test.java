import scala.util.parsing.combinator.testing.Str;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Administrator on 14-6-24.
 */
public class test {
    static ReentrantLock loc = new ReentrantLock();

    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    loc.lockInterruptibly();
                    System.out.println("hello");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally {
                    loc.unlock();
                }

            }
        });

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    loc.lockInterruptibly();
                    //Thread.sleep(10000);
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally {
                    loc.unlock();
                }
            }
        });
        t2.start();
        Thread.sleep(10);
        t1.start();

    }
}
