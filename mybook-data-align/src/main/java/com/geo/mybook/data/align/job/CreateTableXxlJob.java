package com.geo.mybook.data.align.job;


import com.geo.mybook.data.align.constants.TableConstants;
import com.geo.mybook.data.align.mapper.CreateTableMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/*
creator：AZERL7
createTime：15:19
*/
@Component
public class CreateTableXxlJob {
    @Value("${table.shards}")
    private int tableShards;

    @Resource
    private CreateTableMapper createTableMapper;

    //简单任务示例
    @XxlJob("createTableJobHandler")
    public void createTableJobHandler() {
        //表后缀
        String date= LocalDate.now().plusDays(1)//明日故明日
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        XxlJobHelper.log("## 开始创建明日增量数据表...");
        //定时任务
        //分片序号
        if(tableShards>0){
            for (long hashKey = 0; hashKey < tableShards; hashKey++) {
                String tableNameSuffix= TableConstants.buildTableNameSuffix(date,hashKey);
                // 创建表
                createTableMapper.createDataAlignFollowingCountTempTable(tableNameSuffix);
                createTableMapper.createDataAlignFansCountTempTable(tableNameSuffix);
                createTableMapper.createDataAlignNoteCollectCountTempTable(tableNameSuffix);
                createTableMapper.createDataAlignUserCollectCountTempTable(tableNameSuffix);
                createTableMapper.createDataAlignUserLikeCountTempTable(tableNameSuffix);
                createTableMapper.createDataAlignNoteLikeCountTempTable(tableNameSuffix);
                createTableMapper.createDataAlignNotePublishCountTempTable(tableNameSuffix);
            }
        }
        XxlJobHelper.log("## 结束创建日增量数据表，日期：{}...",date);
    }
}
