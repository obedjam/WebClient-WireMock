package com.bankbazaar.webclient.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExecutionRequest implements Serializable {
    private String name;
    private Status status;
}
