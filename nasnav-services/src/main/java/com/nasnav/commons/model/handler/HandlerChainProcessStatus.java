package com.nasnav.commons.model.handler;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class HandlerChainProcessStatus {

    private String id;

    private Status status;

    @Setter
    private Long progress;

    private int totalItem;

    @Setter
    private String currentHandler;

    @JsonIgnore
    private boolean cancelable;


    public void changeStatusToSuccess() {

        status = Status.SUCCESS;
    }

    public void changeStatusToFailed() {

        status = Status.FAILED;
    }

    public void changeStatusToCanceled() {

        status = Status.CANCELED;
    }

    public void changeStatusToInProgress() {

        status = Status.IN_PROGRESS;
    }

    @JsonIgnore
    public boolean isCanceled() {

        return Status.CANCELED.equals(status);
    }

    @JsonIgnore
    public boolean isInProgress() {

        return Status.IN_PROGRESS.equals(status);
    }

    @JsonIgnore
    public boolean isSuccess() {

        return Status.SUCCESS.equals(status);
    }

    public void markAsNotCancelable() {

        this.cancelable = false;
    }

    public boolean isValidToCancelable() {

        return cancelable && isInProgress();
    }

    private enum Status {
        IN_PROGRESS, FAILED, SUCCESS, CANCELED
    }

}


