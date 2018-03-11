package com.oracle.queueservice.model;

public class ReadResponse<T> {
    String elementId;
    T object;

    public ReadResponse(final String elementId, final T object) {
        this.elementId = elementId;
        this.object = object;
    }

    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    public T getObject() {
        return object;
    }

    public void setObject(T object) {
        this.object = object;
    }
}
