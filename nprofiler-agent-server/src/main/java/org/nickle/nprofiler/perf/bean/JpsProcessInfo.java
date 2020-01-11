package org.nickle.nprofiler.perf.bean;

import lombok.Data;

@Data
public class JpsProcessInfo {
    private Integer processId;
    private String mainClass;
    private String description;
}
