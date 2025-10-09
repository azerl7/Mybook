package com.geo.mybook.gateway.exception;


import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.SaTokenException;
import cn.dev33.satoken.stp.SaTokenInfo;
import com.alibaba.nacos.api.naming.pojo.healthcheck.impl.Http;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.geo.framework.common.response.Response;
import com.geo.mybook.gateway.enums.ResponseEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/*
creator：AZERL7
createTime：8:58
*/
@Slf4j
@Component
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        //1、获取响应对象
        ServerHttpResponse response=exchange.getResponse();
        log.error("==> 捕获全局异常",ex);

        //2、捕获异常
        Response<?> result;
        if(ex instanceof NotLoginException){
            //2.1、未登录失败设置401状态码
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            result=Response.fail(ResponseEnum.UNAUTHORIZED.getErrorCode(),ex.getMessage());
        }else if(ex instanceof NotPermissionException){
            //2.2、无权限异常
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            result=Response.fail(ResponseEnum.UNAUTHORIZED.getErrorCode(),ResponseEnum.UNAUTHORIZED.getErrorMessage());
        }
        else {//其他异常统一提示系统繁忙
            result = Response.fail(ResponseEnum.SYSTEM_ERROR.getErrorCode(), ResponseEnum.SYSTEM_ERROR.getErrorMessage());
        }
        //3、设置响应头类型和响应体
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON_UTF8);
        return response.writeWith(Mono.fromSupplier(()->{
            DataBufferFactory bufferFactory=response.bufferFactory();
            try{
                return bufferFactory.wrap(objectMapper.writeValueAsBytes(result));
            }catch(Exception e){
                e.printStackTrace();//如果出现转换错误一并报给前端并提示501
                return bufferFactory.wrap(("501"+e.getMessage()).getBytes(StandardCharsets.UTF_8));
            }
        }));
    }
}
