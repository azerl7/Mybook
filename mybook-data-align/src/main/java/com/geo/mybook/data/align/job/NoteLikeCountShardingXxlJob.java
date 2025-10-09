package com.geo.mybook.data.align.job;


import cn.hutool.core.collection.CollectionUtil;
import com.geo.mybook.data.align.constants.TableConstants;
import com.geo.mybook.data.align.mapper.DeleteTableMapper;
import com.geo.mybook.data.align.mapper.InsertMapper;
import com.geo.mybook.data.align.mapper.SelectMapper;
import com.geo.mybook.data.align.mapper.UpdateMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.geo.framework.common.util.Constants.COUNT_NOTE_KEY_PREFIX;
import static com.geo.framework.common.util.Constants.FIELD_LIKE_TOTAL;

/*
creator：AZERL7
createTime：16:32
*/
@Slf4j
@Component
public class NoteLikeCountShardingXxlJob {

    @Resource
    private SelectMapper selectMapper;

    @Resource
    private DeleteTableMapper deleteTableMapper;

    @Resource
    private UpdateMapper updateMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 分片广播任务
     */
    @XxlJob("noteLikeCountShardingJobHandler")
    public void noteLikeCountShardingJobHandler() throws Exception {
        // 获取分片参数
        // 分片序号
        int shardIndex = XxlJobHelper.getShardIndex();
        // 分片总数
        int shardTotal = XxlJobHelper.getShardTotal();

        XxlJobHelper.log("=================> 开始定时分片广播任务：对当日发生变更的笔记点赞数进行对齐");
        XxlJobHelper.log("分片参数：当前分片序号 = {}, 总分片数 = {}", shardIndex, shardTotal);

        log.info("分片参数：当前分片序号 = {}, 总分片数 = {}", shardIndex, shardTotal);
        //分片计数，数据对齐，和关注数逻辑类似
        //1、分批次查询，统计需要对齐的数据
        String date= LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String tableNameSuffix= TableConstants.buildTableNameSuffix(date, shardIndex);
        int batchSize=1000;//分页池大小
        int processTotal=0;//一共对齐了多少数据
        while(true){
            List<Long> noteIds= selectMapper.selectBatchFromDataAlignNoteLikeCountTempTable(tableNameSuffix,batchSize);
            if(CollectionUtil.isEmpty(noteIds))break;
            noteIds.forEach(noteId->{//2、循环对齐的id，对 note_like 表执行count（*） 操作，获取总数
                //3、更新 note_count 表，并更新对应的redis
                int likeTotal=selectMapper.selectCountFromNoteLikeTableByUserId(noteId);
                updateMapper.updateNoteLikeTotalByUserId(noteId,likeTotal);
                if(likeTotal>0){
                    String redisKey=COUNT_NOTE_KEY_PREFIX+noteId;
                    Boolean isExist = stringRedisTemplate.hasKey(redisKey);
                    if(isExist){
                        stringRedisTemplate.opsForHash().put(redisKey,FIELD_LIKE_TOTAL,likeTotal);
                    }
                }
            });
            processTotal+=noteIds.size();
            //4、批量物理删除记录，避免多次数据对齐
            deleteTableMapper.batchDeleteDataAlignNoteLikeCountTempTable(tableNameSuffix,noteIds);
        }
        XxlJobHelper.log("======>结束定时分片广播任务：对当日发生改变的笔记点赞数进行对齐一共对齐了 {} 条数据 ", processTotal);
    }
}
