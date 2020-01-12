package org.nickle.nprofiler.bean;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CommonResponse<T> {
    public static final Integer SUCCESS_CODE = 200;
    public static final Integer ERROR_CODE = 500;
    public static final CommonResponse SUCCESS = new CommonResponse(SUCCESS_CODE);
    public static final CommonResponse ERROR = new CommonResponse(ERROR_CODE);

    private T msg;
    private Integer code;

    public CommonResponse(Integer code) {
        this.code = code;
    }

}
