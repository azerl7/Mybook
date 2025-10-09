package com.geo.mybook.search.controller;


import com.geo.framework.biz.operationlog.aspect.ApiOperationLog;
import com.geo.mybook.search.service.ExtDictService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
creator：AZERL7
createTime：18:00
*/
@Slf4j
@RestController
@RequestMapping("/search")
public class ExtDictController {
    @Resource
    private ExtDictService extDictService;

    /**
     *
     * @return responseEntity
     */
    @GetMapping("/ext/dict")
    @ApiOperationLog(description = "热更新词典")
    public ResponseEntity<String> extDict() {
        return extDictService.getHotUpdateExtDict();
    }
}
