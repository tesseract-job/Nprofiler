package org.nickle.nprofiler.bean;

import lombok.Data;

/**
 * @author wesley
 * @create 2020-01-12
 */
@Data
public class JstatGCInfo{

    private Integer vmId;
    private double s0c;
    private double s1c;
    private double s0u;
    private double s1u;
    private double ec;
    private double eu;
    private double oc;
    private double ou;
    private double mc;
    private double mu;
    private double ccsc;
    private double ccsu;
    private int ygc;
    private double ygct;
    private int fgc;
    private double fgct;
    private double gct;

    public JstatGCInfo() {
    }

    public JstatGCInfo(String result) {
        String[] strs = result.trim().split("\\s+");
        this.s0c = Double.valueOf(strs[0]);
        this.s1c = Double.valueOf(strs[1]);
        this.s0u = Double.valueOf(strs[2]);
        this.s1u = Double.valueOf(strs[3]);
        this.ec = Double.valueOf(strs[4]);
        this.eu = Double.valueOf(strs[5]);
        this.oc = Double.valueOf(strs[6]);
        this.ou = Double.valueOf(strs[7]);
        this.mc = Double.valueOf(strs[8]);
        this.mu = Double.valueOf(strs[9]);
        this.ccsc = Double.valueOf(strs[10]);
        this.ccsu = Double.valueOf(strs[11]);
        this.ygc = Integer.valueOf(strs[12]);
        this.ygct = Double.valueOf(strs[13]);
        this.fgc = Integer.valueOf(strs[14]);
        this.fgct = Double.valueOf(strs[15]);
        this.gct = Double.valueOf(strs[16]);
    }
}
