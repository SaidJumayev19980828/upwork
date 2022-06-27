package com.nasnav.service.handler;

import com.nasnav.commons.model.handler.HandlerChainProcessStatus;

public interface Handler<T> {

    void handle(T t, HandlerChainProcessStatus status) throws Exception;

    String getName();



}
