package com.geo.mybook.gateway.filter;


import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollectionUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

import static com.geo.framework.common.util.Constants.*;

/*
creator：AZERL7
createTime：9:26
*/
@Slf4j
@Order(-90)
@Component
public class AddUserId2HeaderFilter implements GlobalFilter {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("====> TokenCovertFilter ");

        //自己手动获取token，sa-token初始化有点问题，后面来解决
        // {
        List<String> tokenList = exchange.getRequest().getHeaders().get(TOKEN_HEADER_KEY);
        Long userId = null;
        if (CollectionUtil.isNotEmpty(tokenList)) {
            String tokenAll = tokenList.get(0);
            String token = tokenAll.replace(TOKEN_HEADER_VALUE_PREFIX, "");
            userId = Long.valueOf(Objects.requireNonNull(stringRedisTemplate.opsForValue().get(SA_TOKEN_TOKEN_KEY_PREFIX + token))
            );
        }
        //}
//        Long userId= null;
        try{
            userId=StpUtil.getLoginIdAsLong();
        }catch(Exception e ){
            e.printStackTrace();
            return chain.filter(exchange);//出现错误的话（大概率是没登录，因为前面stpFilter会进行登录校验）交给下一个过滤器保证后面的逻辑继续执行
        }

        log.info("## 当前登录用户ID：{}",userId);
        //修改请求体，添加用户id
        final Long finalUserId = userId;
        ServerWebExchange newExchange=exchange
                .mutate()
                .request(builder-> {
                    builder.header(USER_ID, String.valueOf(finalUserId));
                })
                .build();

        //将请求传递给下一个过滤器
        return chain.filter(newExchange);
    }
}
