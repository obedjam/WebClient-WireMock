package com.bankbazaar.webclient.service.service;

import com.bankbazaar.webclient.core.model.ExecutionRequest;
import com.bankbazaar.webclient.core.model.Status;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ExecutionRequestService {

    public ExecutionRequest updateStatus(ExecutionRequest request, Status status)
    {
        return new ExecutionRequest(request.getName(), status);
    }

    public Boolean isEndState(Status status)
    {
        List<Status> statusList = new ArrayList<>();
        statusList.add(Status.FAILURE);
        statusList.add(Status.ERROR);
        statusList.add(Status.SUCCESS);
        return statusList.contains(status);
    }
}
