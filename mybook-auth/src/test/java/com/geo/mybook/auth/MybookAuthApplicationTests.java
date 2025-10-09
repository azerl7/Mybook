package com.geo.mybook.auth;

//import jakarta.activation.DataSource;
import com.geo.mybook.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;


@SpringBootTest
class MybookAuthApplicationTests {

    @Autowired
    private AuthService AuthService;


    @Autowired
    private DataSource dataSource;


    @Test
    void dataSourceTest(){
        System.out.println(dataSource.getClass().getName());
    }
}
