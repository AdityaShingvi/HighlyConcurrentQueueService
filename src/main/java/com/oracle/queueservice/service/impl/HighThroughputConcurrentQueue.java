package com.oracle.queueservice.service.impl;

import com.oracle.queueservice.model.ReadResponse;
import com.oracle.queueservice.service.IConcurrentQueue;
import com.oracle.queueservice.util.Constants;

import java.util.Hashtable;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A Queue library that supports concurrent Producer Consumer system.
 * Multiple Producers can produce to the Queue at the same time without blocking
 * each other. Similarly, multiple consumers can consume from the queue without
 * blocking each other. The consumers have a timeout associated with their read
 * operation. During this timeout, they can dequeue the object from the queue
 * and until then the object won't be available for subsequent reads for any other
 * consumers. Only after the timeout, the object will be made available again for
 * read if its not already dequeued. Each object is associated with an ElementId,
 * which is used for the dequeue operation by the consumers. This elementId is returned
 * as a part of {@code ReadResponse}to the consumers as a result of the read() call.
 *
 * @param <T>
 */
public class HighThroughputConcurrentQueue<T> implements IConcurrentQueue {

    volatile Random idGenerator;

    final Queue<Object> queue;
    volatile Map<String, Object> elementIdToObjectMap;
    final ScheduledExecutorService watchExecution;

    public HighThroughputConcurrentQueue() {
        this(Constants.THREAD_POOL);
    }

    /**
     * Creates an {@code ConcurrentLinkedQueue} with the given (fixed)
     * capacity and default access policy.
     *
     * @param threadPoolcapacity the capacity of this queue
     * @throws {@code IllegalArgumentException} if {@code capacity < 1}
     */
    public HighThroughputConcurrentQueue(int threadPoolcapacity) throws IllegalArgumentException {
        if (threadPoolcapacity < 1)
            throw new IllegalArgumentException();

        this.idGenerator = new Random();
        this.queue = new ConcurrentLinkedQueue<>();
        this.elementIdToObjectMap = new Hashtable<>();
        this.watchExecution = Executors.newScheduledThreadPool(threadPoolcapacity);
    }

    /**
     * Non-blocking operation to Produce items to the queue.
     * If the param is {@code null}, throws a {@code NullPointerException}
     *
     * @param object An element of Type T that will be added to the queue as a part
     *               of {@code ReadResponse} object.
     */
    @Override
    public boolean enqueue(Object object) {
        if (object == null)
            throw new NullPointerException();

        offer((T) object);
        return true;
    }

    private void offer(T object) {
        String elementId = Constants.ELEMENT_ID_APPENDER + idGenerator.nextInt(1000);
        offer(elementId, object);
    }

    private void offer(String elementId, T object) {
        ReadResponse inputObject = new ReadResponse(elementId, object);
        final Queue<Object> queue = this.queue;
        queue.add(inputObject);
        elementIdToObjectMap.put(elementId, inputObject);
        System.out.println("Enqueued (" + inputObject.getElementId() + ", " + object + ")");
    }

    /**
     * Non-blocking operation to Dequeue the object specified by
     * the {@code elementId} from the queue.
     *
     * @param elementId
     */
    @Override
    public void dequeue(String elementId) {
        poll(elementId);
    }

    private void poll(final String elementId) {
        ReadResponse response = (ReadResponse) elementIdToObjectMap.remove(elementId);
        System.out.println("Dequeued (" + response.getElementId() +", "  + response.getObject() + ")");
    }

    /**
     * Non-blocking Read operation for reading objects from the queue.
     *
     * @param timeout
     * @return
     */
    @Override
    public ReadResponse read(int timeout) {
        if (timeout < 0) return null;

        final Queue<Object> queue = this.queue;
        final ReadResponse response = (ReadResponse) queue.peek();
        if (response == null)
            return null;

        queue.remove();
        System.out.println("Read (" + response.getElementId() +", "  + response.getObject() + ")");

        watchExecution.schedule(
                () -> {
                    if (elementIdToObjectMap.containsKey(response.getElementId())) {
                        elementIdToObjectMap.remove(response.getElementId());
                        enqueue(response.getObject());
                    }
                    else
                        System.out.println("Element (" + response.getElementId() +", "  + response.getObject() + ") already dequeued!");
                    System.out.println("Queue size : " + queue.size());
                    watchExecution.shutdown();
                },
                timeout, TimeUnit.MILLISECONDS);

        return response;
    }
}
