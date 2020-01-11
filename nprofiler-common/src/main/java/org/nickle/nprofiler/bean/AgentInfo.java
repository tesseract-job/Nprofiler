package org.nickle.nprofiler.bean;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AgentInfo {
    private String id;
    private String name;
    @NotBlank
    private String socketInfo;
    private String description;

}
