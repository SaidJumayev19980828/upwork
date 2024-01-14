package com.nasnav.service;

import com.nasnav.dto.response.CallQueueDTO;
import com.nasnav.dto.response.CallQueueStatusDTO;
import com.nasnav.enumerations.CallQueueStatus;
import com.nasnav.response.VideoChatResponse;
import org.springframework.data.domain.PageImpl;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;

public interface CallQueueService {
    /**
     * user enter queue to call specific org
     * @param orgId
     * @return
     */
    public CallQueueStatusDTO enterQueue(Long orgId , Long shopId) throws MessagingException, IOException;

    /**
     * user decided to kick himself out of the queue upon his will
     */
    public void quitQueue();

    /**
     * employee decided to start call with a specific user
     * @param queueId
     * @param force
     * @return
     */
    public VideoChatResponse acceptCall(Long queueId, Boolean force);

    /**
     * employee decided to kick user out of the queue
     * @param queueId
     * @return
     */
    public List<CallQueueDTO> rejectCall(Long queueId , String rejectionReason);

    /**
     * get a full details of queue for employee inside an org
     * @return
     */
    public List<CallQueueDTO> getQueueForEmployee();

    /**
     * user get how far his position in queue is
     * @return
     */
    public CallQueueStatusDTO getQueueStatusForUser();

    /**
     * admin of org get a full details report of calls
     * @param start
     * @param count
     * @param status
     * @return
     */
    public PageImpl<CallQueueDTO> getCallLogs(Integer start, Integer count, CallQueueStatus status);
}
