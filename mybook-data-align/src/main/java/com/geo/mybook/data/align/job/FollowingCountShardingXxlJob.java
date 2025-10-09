package com.geo.mybook.data.align.job;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.db.meta.Table;
import com.geo.mybook.data.align.constants.TableConstants;
import com.geo.mybook.data.align.mapper.DeleteTableMapper;
import com.geo.mybook.data.align.mapper.SelectMapper;
import com.geo.mybook.data.align.mapper.UpdateMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static com.geo.framework.common.util.Constants.COUNT_USER_KEY_PREFIX;
import static com.geo.framework.common.util.Constants.FIELD_FOLLOWING_TOTAL;

/*
creator：AZERL7
createTime：1:07
*/
@Slf4j
@Component
public class FollowingCountShardingXxlJob {

    @Resource
    private SelectMapper selectMapper;
    @Resource
    private UpdateMapper updateMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private DeleteTableMapper deleteTableMapper;

    @XxlJob("followingCountShardingJobHandler")
    public void followingCountShardingXxlJobHandler() {
        //1、获取分片参数
        //1.1、获取分片序号
        int shardIndex= XxlJobHelper.getShardIndex();
        //1.2、获取分片总数
        int shardTotal=XxlJobHelper.getShardTotal();
        XxlJobHelper.log("分片参数：分片序号：{}，分片总数：{}",shardIndex,shardTotal);
        log.info("分片参数：分片序号：{}，分片总数：{}",shardIndex,shardTotal);
        //关注数数据对齐服务
        //1、分批次查询 data_align_following_count_temp**,一次查询1000条，直到查询完（查询大量数据很消耗性能）
        //1.1、构建每张表后缀
        String date= LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));//统计直到昨天的
        String tableNameSuffix= TableConstants.buildTableNameSuffix(date,shardIndex);
        //1.2、一次获取1000条数据
        int batchSize=1000;
        //共对齐了多少条数据,默认为0
        int processedTotal=0;
        while(true){//2、循环发生变更的用户id，对following关注表执行count(1)操作，获取总数
            //获取用户id
            List<Long> userIds=selectMapper.selectBatchFromDataAlignFollowingCountTempTable(tableNameSuffix,batchSize);
            if(CollectionUtil.isEmpty(userIds))break;//直到查询完所有数据
            //根据id进行数据对齐
            userIds.forEach(userId->{
                int followingTotal=selectMapper.selectCountFromFollowingTableByUserId(userId);
                //3、更新用户 user_count 表，并更新对应的redis
                int count=updateMapper.updateUserFollowingTotalByUserId(userId,followingTotal);
                if(count>0){
                    String redisKey=COUNT_USER_KEY_PREFIX+userId;
                    boolean isExist= stringRedisTemplate.hasKey(redisKey);
                    if(isExist){
                        //删除掉也可以，因为需要的时候回去数据库查询的
//                        stringRedisTemplate.delete(redisKey);
                        //你都知道要多少了更新一下redis，可以减少数据库的压力，虽然这个任务一天只会执行一次
                        stringRedisTemplate.opsForHash().put(redisKey,FIELD_FOLLOWING_TOTAL,Long.toString(followingTotal));
                    }
                }
            });
            //4、批量物理删除这一批次的记录（节约资源）
            deleteTableMapper.batchDeleteDataAlignFollowingCountTempTable(tableNameSuffix, userIds);
            processedTotal += userIds.size();
        }
        XxlJobHelper.log("======> 结束分片定时广播任务，对当日发生变更的用户进行对齐，共对齐 {} 条记录}",processedTotal);
    }
}
