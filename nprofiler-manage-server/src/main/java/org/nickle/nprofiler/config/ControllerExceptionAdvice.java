package org.nickle.nprofiler.config;


import lombok.extern.slf4j.Slf4j;
import org.nickle.nprofiler.bean.CommonResponse;
import org.nickle.nprofiler.exception.NprofilerException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
@Slf4j
public class ControllerExceptionAdvice {


    @ExceptionHandler(NprofilerException.class)
    public CommonResponse tesseractExceptionExceptionHandler(NprofilerException e) {
        log.error(e.getMessage());
        return CommonResponse.ERROR.setMsg(e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public CommonResponse tesseractExceptionExceptionHandler(IllegalArgumentException e) {
        log.error(e.getMessage());
        return CommonResponse.ERROR.setMsg("非法入参");
    }


    @ExceptionHandler(Exception.class)
    public CommonResponse commonExceptionHandler(Exception e) {
        log.error(e.toString());
        return CommonResponse.ERROR.setMsg("服务器发生未知异常");
    }


}
