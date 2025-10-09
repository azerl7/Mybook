package com.geo.framewrok.biz.context.filter;


import com.geo.framewrok.biz.context.holder.LoginUserContextHolder;
import io.netty.util.internal.StringUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.geo.framework.common.util.Constants.USER_ID;

/*
creator：AZERL7
createTime：10:33
*/
@Component
public class HeaderUserId2ContextFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //1、获取用户id
        String userId=request.getHeader(USER_ID);
//        System.out.println("全局过滤器已经启用，测试一下看有没有进入");
//        System.out.println("userId");
        //2、判断用户id是否存在
        if(StringUtil.isNullOrEmpty(userId)) {
            //2.1、不存在就放行
            filterChain.doFilter(request,response);
            return ;
        }
        //2.2、存在就存入threadLocal
        LoginUserContextHolder.setUserId(userId);

        try{
            filterChain.doFilter(request,response);
        }finally{//及时删除ThreadLocal避免内存泄露，同时避免脏数据
            LoginUserContextHolder.remove();
        }
    }
}
