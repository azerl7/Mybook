package com.geo.mybook.search.service;


import org.springframework.http.ResponseEntity;

/*
creator：AZERL7
createTime：17:54
*/
public interface ExtDictService {
    /**
     * 获取热更新词典
     * @return response
     */
    ResponseEntity<String> getHotUpdateExtDict();
}
