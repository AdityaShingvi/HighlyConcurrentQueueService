package com.oracle.queueservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ReadResponse<T> {
    @Getter
    String elementId;

    @Getter
    T object;
}
