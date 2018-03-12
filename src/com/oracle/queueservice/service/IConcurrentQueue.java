package com.oracle.queueservice.service;

import com.oracle.queueservice.model.ReadResponse;

public interface IConcurrentQueue<T> {

    /**
     * enqueue.
     * @param object T
     */
    boolean enqueue(T object);

    /**
     * read.
     * @return @link{ReadResponse}
     */
    ReadResponse read();

    /**
     * dequeue.
     * @param elementId
     */
    void dequeue(String elementId);

    /**
     *
     * @param timeout
     */
    ReadResponse read(int timeout);
}
