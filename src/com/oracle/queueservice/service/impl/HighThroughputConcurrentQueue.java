package com.oracle.queueservice.service.impl;

import com.oracle.queueservice.model.ReadResponse;
import com.oracle.queueservice.service.IConcurrentQueue;

import java.util.Hashtable;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class HighThroughputConcurrentQueue<T> implements IConcurrentQueue {

    final ReentrantLock lock;
    final Random idGenerator;

    volatile Queue<Object> queue;
    volatile Map<String, Object> elementIdToObjectMap;
    final ScheduledExecutorService watchExecution;

    public HighThroughputConcurrentQueue() {
        this(1000);
    }

    /**
     * Creates an {@code ArrayBlockingQueue} with the given (fixed)
     * capacity and default access policy.
     *
     * @param capacity the capacity of this queue
     * @throws IllegalArgumentException if {@code capacity < 1}
     */
    public HighThroughputConcurrentQueue(int capacity) {
        if (capacity < 1)
            throw new IllegalArgumentException();
        this.idGenerator = new Random();
        this.lock = new ReentrantLock(true);
        this.queue = new ConcurrentLinkedQueue<>();
        this.elementIdToObjectMap = new Hashtable<>();
        this.watchExecution = Executors.newScheduledThreadPool(capacity);
    }

    /**
     * enqueue.
     *
     * @param object T
     */
    @Override
    public boolean enqueue(Object object) {
        if (object == null)
            throw new NullPointerException();
            offer((T) object);
            return true;
    }

    private void offer(T object) {
        String elementId = "e-" + idGenerator.nextInt(1000);
        offer(elementId, object);
    }

    private void offer(String elementId, T object) {
        ReadResponse inputObject = new ReadResponse(elementId, object);
        final Queue<Object> queue = this.queue;
        queue.add(inputObject);
        elementIdToObjectMap.put(elementId, inputObject);
        System.out.println("Enqueued element " + object );
    }

    /**
     * dequeue.
     *
     * @param elementId
     */
    @Override
    public void dequeue(String elementId) {
        poll(elementId);
    }

    private void poll(final String elementId) {
        ReadResponse response = (ReadResponse) elementIdToObjectMap.remove(elementId);
        System.out.println("Dequeued element " + response.getObject());
    }

    /**
     * read.
     *
     * //@param timeout in seconds
     * @return @link{ReadResponse}
     */
    @Override
    public ReadResponse read() {
        ReentrantLock lock = this.lock;
        final Queue<Object> queue = this.queue;
        lock.lock();
        try {
            ReadResponse response = (ReadResponse) queue.peek();
            if (response == null)
                return null;
            response = (ReadResponse) queue.remove();
            System.out.println("Read element " + response.getObject());
            return response;
        } finally {
            lock.unlock();
        }
    }

    /**
     *
     * @param timeout
     * @return
     */
    @Override
    public ReadResponse read(int timeout) {
        final Queue<Object> queue = this.queue;
        try {
            final ReadResponse response = (ReadResponse) queue.peek();
            if (response == null)
                return null;
            queue.remove();
            System.out.println("Read element " + response.getObject());

            watchExecution.schedule(
                    () -> {
                        System.out.println("Watch Deamon executing");
                        if (elementIdToObjectMap.containsKey(response.getElementId()))
                            queue.add(response);
                        else
                            System.out.println("Element " + response.getObject() + " already dequeued.");
                        System.out.println("Queue size : " + queue.size());
                        watchExecution.shutdown();
                    },
                    timeout, TimeUnit.MILLISECONDS);

            return response;
        } catch (Exception e) {

        }
        return null;
    }
}
