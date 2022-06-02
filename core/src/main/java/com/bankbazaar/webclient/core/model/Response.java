package com.bankbazaar.webclient.core.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Response implements Serializable {
    private String name;
    private Status status;
    @JsonFormat(pattern="MMM dd, yyyy, HH:mm:ss aa")
    private Date createdOn;
}
