package com.nasnav.service.handler;


import com.esotericsoftware.minlog.Log;
import com.nasnav.commons.model.handler.HandlerChainProcessStatus;
import com.nasnav.exceptions.ErrorCodes;
import com.nasnav.exceptions.ProcessCancelException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@Slf4j
public abstract class HandlingChainingProcess<T> implements Runnable {

    @Getter
    private final HandlerChainProcessStatus currentStatus;

    @Getter
    private final T processData;

    private final List<Handler<T>> handlers;


    public HandlingChainingProcess(final T processData, List<Handler<T>> handlers) {

        currentStatus = HandlerChainProcessStatus.builder()
                .id(UUID.randomUUID().toString())
                .progress(0L)
                .cancelable(true)
                .totalItem(handlers.size())
                .build();
        currentStatus.changeStatusToInProgress();

        this.processData = processData;
        this.handlers = handlers;
    }

    @Override
    public void run() {

        for (Handler<T> handler : handlers) {

            if (currentStatus.isCanceled()) {
                handleCancelProcess();
                break;
            }

            updateProgress(handler.getName());

            try {
                long startTime = System.currentTimeMillis();
                Log.info("Starting Handler --------------------------------------------> " + handler.getName());
                handler.handle(processData, currentStatus);
                Log.info("End Handler -------------------------------------------------> " + handler.getName()
                        + " in time millisecond = " + (System.currentTimeMillis() - startTime));
            } catch (Exception e) {
                log.error("Error in handler name " + handler.getName(), e);
                currentStatus.changeStatusToFailed();
                break;
            }

        }

        if (currentStatus.isInProgress())
            currentStatus.changeStatusToSuccess();
    }

    private void updateProgress(final String handlerName) {

        currentStatus.setProgress(currentStatus.getProgress() + 1);
        currentStatus.setCurrentHandler(handlerName);
    }

    public void cancelProcess() throws ProcessCancelException {

        if (!currentStatus.isValidToCancelable())
            throw new ProcessCancelException(ErrorCodes.PROCESS$CANCEL$0001);
        currentStatus.changeStatusToCanceled();
    }

    public abstract void handleCancelProcess();

    public abstract Object getResult();


}
