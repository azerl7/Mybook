package com.geo.framework.biz.operationlog.aspect;


import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

/*
creator：AZERL7
createTime：17:49
*/
@Aspect
@Slf4j
public class ApiOperationLogAspect {

    // 以自定义 @ApiOperationLog 注解为切点，凡是添加 @ApiOperationLog 的方法，都会执行环绕中的代码
    @Pointcut("@annotation(com.geo.framework.biz.operationlog.aspect.ApiOperationLog)")
    public void apiOperationLog() {}


    @Around("apiOperationLog()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        //1、记录请求开始时间
        long startTime = System.currentTimeMillis();
        //2、获取被请求的类和方法
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        //3、获取参数
        Object[] args=joinPoint.getArgs();
        //4、参数转为json字符串
        String argJson= JSONUtil.toJsonStr(args);

        //5、功能描述信息
        String description=getApiOperationLogDescription(joinPoint);

        //6、输出日志信息
        log.info(" ===== 请求开始：[{}]，入参：{}，请求类：{}，请求方法：{} ====="
                ,description,argJson,className,methodName);

        //7、执行切面方法
        Object result = joinPoint.proceed();

        //8、记录请求结束时间
        long  endTime = System.currentTimeMillis();

        //3、总共耗时
        long totalTime = endTime - startTime;
        log.info(" ===== 运行结束：[{}]，耗时：{}ms，出参：{} ====="
                ,description,totalTime,JSONUtil.toJsonStr(result));
        return result;
    }
    private String getApiOperationLogDescription(ProceedingJoinPoint joinPoint) {
        // 1. 从 ProceedingJoinPoint 获取 MethodSignature
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        // 2. 使用 MethodSignature 获取当前被注解的 Method
        Method method = signature.getMethod();

        // 3. 从 Method 中提取 LogExecution 注解
        ApiOperationLog apiOperationLog = method.getAnnotation(ApiOperationLog.class);

        // 4. 从 LogExecution 注解中获取 description 属性
        return apiOperationLog.description();
    }
}
