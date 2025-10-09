package com.geo.mybook.data.align.job;


/*
creator：AZERL7
createTime：17:18
*/

import com.geo.mybook.data.align.constants.TableConstants;
import com.geo.mybook.data.align.mapper.DeleteTableMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class DeleteTableXxlJob {

    @Value("${table.shards}")
    private int tableShards;

    @Resource
    private DeleteTableMapper deleteTableMapper;

    //简单任务示例
    @XxlJob("deleteTableJobHandler")
    public void deleteTableXxlJobHandler() {
        XxlJobHelper.log("## 开始删除近一个月的增量表");
        LocalDate today=LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate startDate=today;
        //往前推一个月
        LocalDate endDate=today.minusDays(1);

        //循环最近一个月不包含今天
        while(startDate.isAfter(endDate)){
            startDate=startDate.minusDays(1);
            String date = startDate.format(formatter);
            for (int hashKey = 0; hashKey < tableShards; hashKey++) {
                // 表名后缀
                String tableNameSuffix = TableConstants.buildTableNameSuffix(date, hashKey);
                XxlJobHelper.log("删除表后缀: {}", tableNameSuffix);

                // 删除表
                deleteTableMapper.deleteDataAlignFollowingCountTempTable(tableNameSuffix);
                deleteTableMapper.deleteDataAlignFansCountTempTable(tableNameSuffix);
                deleteTableMapper.deleteDataAlignNoteCollectCountTempTable(tableNameSuffix);
                deleteTableMapper.deleteDataAlignUserCollectCountTempTable(tableNameSuffix);
                deleteTableMapper.deleteDataAlignUserLikeCountTempTable(tableNameSuffix);
                deleteTableMapper.deleteDataAlignNoteLikeCountTempTable(tableNameSuffix);
                deleteTableMapper.deleteDataAlignNotePublishCountTempTable(tableNameSuffix);
            }
        }
        XxlJobHelper.log("## 结束删除最近一个月的日增量临时表");
    }
}
