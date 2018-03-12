package com.oracle.queueservice.actors;

import com.oracle.queueservice.model.ReadResponse;
import com.oracle.queueservice.service.IConcurrentQueue;

import java.util.concurrent.Callable;

public class Consumer<T> implements Callable<T> {

    private int id;
    private IConcurrentQueue<T> queue;
    private int timeout;

    public Consumer(final int id, final IConcurrentQueue queue, final int timeout) {
        this.id = id;
        this.queue = queue;
        this.timeout = timeout;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public T call() {
        ReadResponse response = queue.read(timeout);
        if (response != null) {
            if (id % 5 == 0) {
                try {
                    Thread.sleep(2000);
                    queue.dequeue(response.getElementId());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
//            System.out.println("Consumer " + id + " consuming data " + response.getObject().toString());
        }
        else
            System.out.println("Consumer " + id + ", nothing to Consume ");

        return (T) response.getObject();
    }
}
