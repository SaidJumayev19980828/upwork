package com.nasnav.commons.model.handler;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class HandlerChainProcessStatus {

    private String id;

    private Status status;

    @Setter
    private Long progress;

    private int totalItems;

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
    public boolean isFailed() {

        return Status.FAILED.equals(status);
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

    @JsonIgnore
    public boolean isNotValidToClear() {

        return isInProgress();
    }

    private enum Status {
        IN_PROGRESS, FAILED, SUCCESS, CANCELED
    }

}


