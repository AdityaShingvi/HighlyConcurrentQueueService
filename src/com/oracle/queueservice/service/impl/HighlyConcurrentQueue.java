package com.oracle.queueservice.service.impl;

import com.oracle.queueservice.model.ReadResponse;
import com.oracle.queueservice.service.IConcurrentQueue;

import java.util.Hashtable;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class HighlyConcurrentQueue<T> implements IConcurrentQueue {

    final Object[] highlyConcurrentQueue;
    final Map<String, Integer> elementIdToQueueIndexMap;
    final Map<String, Boolean> isElementReadableMap;
    final PriorityQueue<Integer> emptyIndexes;

    int nextReadIndex;
    int nextWriteIndex;
    int count;

    final ReentrantLock lock;

    Random idGenerator;

    public HighlyConcurrentQueue() {
        this(100);
    }

    /**
     * Creates an {@code ArrayBlockingQueue} with the given (fixed)
     * capacity and default access policy.
     *
     * @param capacity the capacity of this queue
     * @throws IllegalArgumentException if {@code capacity < 1}
     */
    public HighlyConcurrentQueue(int capacity) {
        if (capacity < 1)
            throw new IllegalArgumentException();

        this.highlyConcurrentQueue = new Object[capacity];
        this.idGenerator = new Random();
        this.elementIdToQueueIndexMap = new Hashtable<>();
        this.isElementReadableMap = new Hashtable<>();
        this.emptyIndexes = new PriorityQueue<>();
        this.lock = new ReentrantLock(true);
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

        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (count == highlyConcurrentQueue.length)
                return false;
            else {
                offer((T) object);
                return true;
            }
        } finally {
            lock.unlock();
        }
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

    private void offer(T object) {
        String elementId = "e-" + idGenerator.nextInt(1000);
        offer(elementId, object);
    }

    private void offer(String elementId, T object) {
        ReadResponse inputObject = new ReadResponse(elementId, object);
        final Object[] queue = this.highlyConcurrentQueue;
        queue[nextWriteIndex] = inputObject;
        System.out.println("Enqueued element " + object + " at position " + nextWriteIndex);
        elementIdToQueueIndexMap.put(elementId, nextWriteIndex);
        System.out.println("Mapped " + elementId + " -> " + nextWriteIndex);
        count++;
        if (++nextWriteIndex == queue.length && count < queue.length) {
            nextWriteIndex = emptyIndexes.poll();
        }
    }

    private void poll(final String elementId) {
        final Object[] queue = this.highlyConcurrentQueue;
        int elementIndex = elementIdToQueueIndexMap.remove(elementId);
        ReadResponse element = (ReadResponse) queue[elementIndex];
        queue[elementIndex] = null;
        emptyIndexes.add(elementIndex);
        System.out.println("Dequeued element " + element.getObject() +  " at position " + nextReadIndex);

        // I guess this doesn't needs to be handled here. read() takes care of it and that's enough ?
//        if (++nextReadIndex == queue.length) {
//            // TODO: Check if count != queue.length, then set nextReadIndex to next non null element in the array
//            nextReadIndex = 0;
//        }
        count--;
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
        final Object[] queue = this.highlyConcurrentQueue;
        lock.lock();
        try {
                ReadResponse readResponse = (ReadResponse) queue[nextReadIndex];
                //isElementReadableMap.put(readResponse.getElementId(), false);
                System.out.println("Read element " + readResponse.getObject() + " at position " + nextReadIndex);
                queue[nextReadIndex] = null;
                emptyIndexes.add(nextReadIndex);
                if (++nextReadIndex == queue.length && count > 0)
                    nextReadIndex = 0;
                offer(readResponse.getElementId(), (T) readResponse.getObject());
                return readResponse;
        } finally {
            lock.unlock();
        }
    }

    private boolean isReadable(Object o) {
        ReadResponse response = (ReadResponse) o;
        if (o == null) return false;
        if (isElementReadableMap.get(response.getElementId())) return true;
        return false;
    }
}
