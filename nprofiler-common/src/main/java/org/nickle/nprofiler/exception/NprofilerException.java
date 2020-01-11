package org.nickle.nprofiler.exception;

import lombok.Data;

@Data
public class NprofilerException extends RuntimeException {
    public static final int DEFAULT_ERR_CODE = 500;
    private int errCode;

    public NprofilerException(String msg) {
        super(msg);
        this.errCode = DEFAULT_ERR_CODE;
    }

    public NprofilerException(String msg, int code) {
        super(msg);
        this.errCode = code;
    }
}
