
package com.bankbazaar.webclient.core.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rating implements Serializable
{
    @JsonProperty(value = "Source")
    @SerializedName(value = "Source")
    private String source;
    @JsonProperty(value = "Value")
    @SerializedName(value = "Value")
    private String value;
    private final static long serialVersionUID = 9154863854220400256L;

}
