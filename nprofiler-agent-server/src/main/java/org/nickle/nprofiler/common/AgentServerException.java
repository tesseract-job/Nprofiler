package org.nickle.nprofiler.common;

import lombok.Data;

@Data
public class AgentServerException extends RuntimeException {
    public static final int DEFAULT_ERR_CODE = 500;
    private int errCode;

    public AgentServerException(String msg) {
        super(msg);
        this.errCode = DEFAULT_ERR_CODE;
    }

    public AgentServerException(String msg, int code) {
        super(msg);
        this.errCode = code;
    }
}
