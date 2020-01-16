package org.nickle.nprofiler.bean;

import lombok.Data;

/**
 * class概要
 * @author wesley
 * @create 2020-01-16
 */
@Data
public class HistogramInfo {

    private String ClassName;
    private int count;
    private long size;

}
