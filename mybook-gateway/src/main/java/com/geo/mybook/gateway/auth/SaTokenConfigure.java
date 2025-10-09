package com.geo.mybook.gateway.auth;


import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/*
creator：AZERL7
createTime：16:29
*/
//@Order(-100)
@Configuration
public class SaTokenConfigure {

    //注册SaToken全局过滤器
    @Bean
    public SaReactorFilter getSaReactorFilter(){
        return new SaReactorFilter()
                .addInclude("/**")
                .addExclude("/favicon.ioc")
                .setAuth(obj->{
                    //登录校验
                    SaRouter.match("/**")
                            .notMatch("/auth/login/**")
                            .notMatch("/auth/verification/code/send/**")
                            .check(r->StpUtil.checkLogin());
                    //权限校验
                    SaRouter.match("/auth/logout",r->StpUtil.checkRole("common_user"));
                    SaRouter.match("/auth/logout",r->StpUtil.checkPermission("app:note:publish"));
                })
                .setError(e->{
//                    e.printStackTrace();
//                    return SaResult.error(e.getMessage());
                    // 手动抛出异常，抛给全局异常处理器
                    if (e instanceof NotLoginException) { // 未登录异常
                        throw new NotLoginException(e.getMessage(), null, null);
                    } else if (e instanceof NotPermissionException || e instanceof NotRoleException) { // 权限不足，或不具备角色，统一抛出权限不足异常
                        throw new NotPermissionException(e.getMessage());
                    } else { // 其他异常，则抛出一个运行时异常
                        throw new RuntimeException(e.getMessage());
                    }
                });
    }
}
