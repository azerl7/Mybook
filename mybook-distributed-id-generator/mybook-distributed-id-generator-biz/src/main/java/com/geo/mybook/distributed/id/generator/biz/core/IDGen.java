package com.geo.mybook.distributed.id.generator.biz.core;


import com.geo.mybook.distributed.id.generator.biz.core.common.Result;

public interface IDGen {
    Result get(String key);
    boolean init();
}
