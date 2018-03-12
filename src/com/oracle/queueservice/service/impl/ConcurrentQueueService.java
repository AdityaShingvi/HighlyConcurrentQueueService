package com.oracle.queueservice.service.impl;

import com.oracle.queueservice.model.ReadResponse;
import com.oracle.queueservice.service.IConcurrentQueue;

import java.util.Deque;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentQueueService<T> implements IConcurrentQueue {

    final Object[] highlyConcurrentQueue;
    final Map<String, Integer> elementIdToQueueIndexMap;
    final Map<String, Boolean> isElementReadableMap;
    final PriorityQueue<Integer> emptyIndexes;
    int count;
    final ReentrantLock lock;
    Random idGenerator;

    // Deque
    final Deque<Object> queue;
    final Map<String, Object> elementIdToObjectMap;
    final Object nextRead;

    public ConcurrentQueueService() {
        this(100);
    }

    /**
     * Creates an {@code ArrayBlockingQueue} with the given (fixed)
     * capacity and default access policy.
     *
     * @param capacity the capacity of this queue
     * @throws IllegalArgumentException if {@code capacity < 1}
     */
    public ConcurrentQueueService(int capacity) {
        if (capacity < 1)
            throw new IllegalArgumentException();

        this.highlyConcurrentQueue = new Object[capacity];
        this.idGenerator = new Random();
        this.elementIdToQueueIndexMap = new Hashtable<>();
        this.isElementReadableMap = new Hashtable<>();
        this.emptyIndexes = new PriorityQueue<>();
        this.lock = new ReentrantLock(true);
        this.queue = new LinkedList<>();
        this.elementIdToObjectMap = new Hashtable<>();
        this.nextRead = null;
    }

    /* Using List */

    /**
     * enqueue.
     *
     * @param object T
     */
    @Override
    public boolean enqueue(Object object) {
        if (object == null)
            throw new NullPointerException();

        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            offer((T) object);
            return true;
        } finally {
            lock.unlock();
        }
    }

    private void offer(T object) {
        String elementId = "e-" + idGenerator.nextInt(1000);
        offer(elementId, object);
    }

    private void offer(String elementId, T object) {
        ReadResponse inputObject = new ReadResponse(elementId, object);
        final Deque<Object> queue = this.queue;
        queue.add(inputObject);
        elementIdToObjectMap.put(elementId, inputObject);
        count++;
        System.out.println("Enqueued element " + object );
    }

    /**
     * dequeue.
     *
     * @param elementId
     */
    @Override
    public void dequeue(String elementId) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (count == 0)
                return;
            else
                poll(elementId);
        } finally {
            lock.unlock();
        }
    }

    private void poll(final String elementId) {
        final Deque<Object> queue = this.queue;
        ReadResponse response = (ReadResponse) elementIdToObjectMap.remove(elementId);
        boolean isSuccess = queue.remove(response);
        if (isSuccess) {
            count--;
            isElementReadableMap.remove(elementId);
        }
        System.out.println("Dequeued element " + response.getObject() + ". Status: " + isSuccess);
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
        final Deque<Object> queue = this.queue;
        lock.lock();
        try {
            ReadResponse response = (ReadResponse) queue.peek();
            if (response == null)
                return null;

            System.out.println("Reading " + response.getObject());
            if (isElementReadableMap.containsKey(response.getElementId()))
                return null;
            if (queue.size() > 1) {
                System.out.println("Rearranging queue..");
                response = (ReadResponse) queue.remove();
                count--;
                offer(response.getElementId(), (T) response.getObject());
            }
            isElementReadableMap.put(response.getElementId(), false);
            System.out.println("Read element " + response.getObject());
            return response;
        } finally {
            lock.unlock();
        }
    }

    /**
     * @param timeout
     */
    @Override
    public ReadResponse read(int timeout) {
        return null;
    }
}
