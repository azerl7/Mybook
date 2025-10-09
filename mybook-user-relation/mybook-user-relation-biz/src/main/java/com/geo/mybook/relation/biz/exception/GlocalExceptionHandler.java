package com.geo.mybook.relation.biz.exception;


import com.geo.framework.common.exception.BizException;
import com.geo.framework.common.response.Response;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;

import static com.geo.mybook.relation.biz.enums.ResponseEnum.PARAM_NOT_VALID;
import static com.geo.mybook.relation.biz.enums.ResponseEnum.SYSTEM_ERROR;

/*
creator：AZERL7
createTime：14:47
*/
@Slf4j
@ControllerAdvice
public class GlocalExceptionHandler {

    @ResponseBody
    @ExceptionHandler({BizException.class})
    public Response<Object> handleBizException(HttpServletRequest request, BizException e){
        log.warn("{} request fail, errorCode: {}, errorMessage: {}",request.getRequestURI(),e.getErrorCode(),e.getErrorMessage());
        return Response.fail(e.getErrorCode(),e.getErrorMessage());
    }

    public Response<Object> handleMethodArgumentNotValidException(HttpServletRequest request, MethodArgumentNotValidException e){
        String errorCode=PARAM_NOT_VALID.getErrorCode();

        BindingResult bindingResult=e.getBindingResult();

        StringBuilder sb=new StringBuilder();

        Optional.ofNullable(bindingResult.getFieldErrors()).ifPresent(errors->{
            errors.forEach(
                    error->{
                        sb.append(" ")
                                .append(error.getDefaultMessage())
                                .append(", 当前值： '")
                                .append(error.getRejectedValue())
                                .append("';");
                    }
            );
        });
        String errorMessage=sb.toString();
        log.warn("{} request error，errorCode: {},errorMessage: {}",request.getRequestURI(),errorCode,errorMessage);
        return Response.fail(errorCode,errorMessage);
    }

    @ExceptionHandler({ IllegalArgumentException.class })
    @ResponseBody
    public Response<Object> handleIllegalArgumentException(HttpServletRequest request, IllegalArgumentException e) {
        String errorCode = PARAM_NOT_VALID.getErrorCode();
        String errorMessage = e.getMessage();
        log.warn("{} request error, errorCode: {}, errorMessage: {}", request.getRequestURI(), errorCode, errorMessage);
        return Response.fail(errorCode, errorMessage);
    }

    @ExceptionHandler({ Exception.class })
    @ResponseBody
    public Response<Object> handleOtherException(HttpServletRequest request, Exception e) {
        log.error("{} request error, ", request.getRequestURI(), e);
        return Response.fail(SYSTEM_ERROR.getErrorCode(),SYSTEM_ERROR.getErrorMessage());
    }
}
