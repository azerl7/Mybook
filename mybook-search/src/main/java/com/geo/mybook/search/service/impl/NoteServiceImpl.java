package com.geo.mybook.search.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import com.geo.framework.common.response.PageResponse;
import com.geo.framework.common.util.DateUtils;
import com.geo.framework.common.util.NumberUtils;
import com.geo.mybook.search.domain.vo.SearchNoteReqVO;
import com.geo.mybook.search.domain.vo.SearchNoteResVO;
import com.geo.mybook.search.enums.NotePublishTimeRangeEnum;
import com.geo.mybook.search.enums.NoteSortTypeEnum;
import com.geo.mybook.search.index.NoteIndex;
import com.geo.mybook.search.service.NoteService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.lucene.search.function.FieldValueFactorFunction;
import org.elasticsearch.common.lucene.search.function.FunctionScoreQuery;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FieldValueFactorFunctionBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/*
creator：AZERL7
createTime：16:31
*/
@Slf4j
@Service
public class NoteServiceImpl implements NoteService {

    @Resource
    private RestHighLevelClient restHighLevelClient;

    @Override
    public PageResponse<SearchNoteResVO> searchNote(SearchNoteReqVO searchNoteReqVO) {

        //1、获取查询参数
        String keyword = searchNoteReqVO.getKeyword();
        // 当前页码
        Integer pageNo = searchNoteReqVO.getPageNo();
        // 笔记类型
        Integer type = searchNoteReqVO.getType();
        //排序类型
        Integer sort=searchNoteReqVO.getSort();
        // 发布时间范围
        Integer publishTimeRange = searchNoteReqVO.getPublishTimeRange();

        //2、构建查询请求
        // 构建 SearchRequest，指定要查询的索引
        SearchRequest searchRequest = new SearchRequest(NoteIndex.NAME);

        //2.1、 创建查询构建器
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        //2.1、 创建查询条件
        //       "query": {
        //         "multi_match": {
        //           "query": "壁纸",
        //           "fields": ["title^2", "topic"]
        //         }
        //       },
        // 创建查询条件
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().must(
                QueryBuilders.multiMatchQuery(keyword)
                        .field(NoteIndex.FIELD_NOTE_TITLE, 2.0f) // 手动设置笔记标题的权重值为 2.0
                        .field(NoteIndex.FIELD_NOTE_TOPIC) // 不设置，权重默认为 1.0
        );

        if(ObjectUtil.isNotNull(type)){//手动勾选了，则进行添加
            boolQueryBuilder.filter(QueryBuilders.termQuery(NoteIndex.FIELD_NOTE_TYPE, type));
        }

        //按照发布时间过滤
        NotePublishTimeRangeEnum notePublishTimeRangeEnum = NotePublishTimeRangeEnum.valueOf(publishTimeRange);
        if(ObjectUtil.isNotNull(notePublishTimeRangeEnum)){
            // 结束时间
            String endTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            // 开始时间
            String startTime = null;

            switch (notePublishTimeRangeEnum) {
                case DAY ->
                        startTime = DateUtils.localDateTime2String(LocalDateTime.now().minusDays(1)); // 一天之前的时间
                case WEEK ->
                        startTime = DateUtils.localDateTime2String(LocalDateTime.now().minusWeeks(1)); // 一周之前的时间
                case HALF_YEAR ->
                        startTime = DateUtils.localDateTime2String(LocalDateTime.now().minusMonths(6)); // 半年之前的时间
            }
            // 设置时间范围
            if (StringUtils.isNoneBlank(startTime)) {
                boolQueryBuilder.filter(QueryBuilders.rangeQuery(NoteIndex.FIELD_NOTE_CREATE_TIME)
                        .gte(startTime) // 大于等于
                        .lte(endTime) // 小于等于
                );
            }
        }

        // 排序
        NoteSortTypeEnum noteSortTypeEnum = NoteSortTypeEnum.valueOf(sort);
        if(ObjectUtil.isNotNull(noteSortTypeEnum)){
            switch (noteSortTypeEnum) {
                // 按笔记发布时间降序
                case LATEST -> sourceBuilder.sort(new FieldSortBuilder(NoteIndex.FIELD_NOTE_CREATE_TIME).order(SortOrder.DESC));
                // 按笔记点赞量降序
                case MOST_LIKE -> sourceBuilder.sort(new FieldSortBuilder(NoteIndex.FIELD_NOTE_LIKE_TOTAL).order(SortOrder.DESC));
                // 按评论量降序
                case MOST_COMMENT -> sourceBuilder.sort(new FieldSortBuilder(NoteIndex.FIELD_NOTE_COMMENT_TOTAL).order(SortOrder.DESC));
                // 按收藏量降序
                case MOST_COLLECT -> sourceBuilder.sort(new FieldSortBuilder(NoteIndex.FIELD_NOTE_COLLECT_TOTAL).order(SortOrder.DESC));
            }
            sourceBuilder.query(boolQueryBuilder);
        }else{

            //2.3、 创建 FilterFunctionBuilder 数组
            // "functions": [
            //         {
            //           "field_value_factor": {
            //             "field": "like_total",
            //             "factor": 0.5,
            //             "modifier": "sqrt",
            //             "missing": 0
            //           }
            //         },
            //         {
            //           "field_value_factor": {
            //             "field": "collect_total",
            //             "factor": 0.3,
            //             "modifier": "sqrt",
            //             "missing": 0
            //           }
            //         },
            //         {
            //           "field_value_factor": {
            //             "field": "comment_total",
            //             "factor": 0.2,
            //             "modifier": "sqrt",
            //             "missing": 0
            //           }
            //         }
            //       ],
            FunctionScoreQueryBuilder.FilterFunctionBuilder[] filterFunctionBuilders = new FunctionScoreQueryBuilder.FilterFunctionBuilder[] {
                    // function 1
                    new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                            new FieldValueFactorFunctionBuilder(NoteIndex.FIELD_NOTE_LIKE_TOTAL)
                                    .factor(0.5f)
                                    .modifier(FieldValueFactorFunction.Modifier.SQRT)
                                    .missing(0)
                    ),
                    // function 2
                    new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                            new FieldValueFactorFunctionBuilder(NoteIndex.FIELD_NOTE_COLLECT_TOTAL)
                                    .factor(0.3f)
                                    .modifier(FieldValueFactorFunction.Modifier.SQRT)
                                    .missing(0)
                    ),
                    // function 3
                    new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                            new FieldValueFactorFunctionBuilder(NoteIndex.FIELD_NOTE_COMMENT_TOTAL)
                                    .factor(0.2f)
                                    .modifier(FieldValueFactorFunction.Modifier.SQRT)
                                    .missing(0)
                    )
            };
            //2.4、 构建 function_score 查询
            // "score_mode": "sum",
            // "boost_mode": "sum"
            FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery(boolQueryBuilder,
                            filterFunctionBuilders)
                    .scoreMode(FunctionScoreQuery.ScoreMode.SUM) // score_mode 为 sum
                    .boostMode(CombineFunction.SUM); // boost_mode 为 sum
            sourceBuilder.sort(new FieldSortBuilder("_score").order(SortOrder.DESC)); // 按照 _score 降序
            //2.5、 设置查询
            sourceBuilder.query(functionScoreQueryBuilder);
        }
        // 设置排序
        // "sort": [
        //     {
        //       "_score": {
        //         "order": "desc"
        //       }
        //     }
        //   ]

        //2.6、设置分页
        int pageSize = 10; // 每页展示数据量
        int from = (pageNo - 1) * pageSize; // 偏移量
        sourceBuilder.from(from);
        sourceBuilder.size(pageSize);
        //2.7、设置高亮字段
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field(NoteIndex.FIELD_NOTE_TITLE)
                .preTags("<strong>") // 设置包裹标签
                .postTags("</strong>");
        sourceBuilder.highlighter(highlightBuilder);

        // 将构建的查询条件设置到 SearchRequest 中
        searchRequest.source(sourceBuilder);

        //3、执行请求
        List<SearchNoteResVO> searchNoteResVOS = Lists.newArrayList();
        long total=0;
        try{
            log.info("==> SearchRequest:{}",searchRequest);
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            //处理搜索结果
            SearchHits searchHits = searchResponse.getHits();
            total=searchHits.getTotalHits().value;
            log.info("==> 文档命中数：totla: {}",total);
            for(SearchHit hit : searchHits.getHits()) {
                //4、获取命中的文档信息
                log.info("==> 文档数据：{}",hit.getSourceAsString());
                //获取文档所有对应字段的值
                Map<String,Object> sourceAsMap = hit.getSourceAsMap();
                Long noteId = (Long) sourceAsMap.get(NoteIndex.FIELD_NOTE_ID);
                String cover = (String) sourceAsMap.get(NoteIndex.FIELD_NOTE_COVER);
                String title = (String) sourceAsMap.get(NoteIndex.FIELD_NOTE_TITLE);
                String avatar = (String) sourceAsMap.get(NoteIndex.FIELD_NOTE_AVATAR);
                String nickname = (String) sourceAsMap.get(NoteIndex.FIELD_NOTE_NICKNAME);
                // 获取更新时间
                String updateTimeStr = (String) sourceAsMap.get(NoteIndex.FIELD_NOTE_UPDATE_TIME);
                LocalDateTime updateTime = LocalDateTime.parse(updateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                Integer likeTotal = (Integer) sourceAsMap.get(NoteIndex.FIELD_NOTE_LIKE_TOTAL);
                Integer commentTotal = (Integer) sourceAsMap.get(NoteIndex.FIELD_NOTE_COMMENT_TOTAL);
                Integer collectTotal = (Integer) sourceAsMap.get(NoteIndex.FIELD_NOTE_COLLECT_TOTAL);

                //获取高亮字段
                String highlightedTitle=null;
                if (CollectionUtil.isNotEmpty(hit.getHighlightFields())
                        && hit.getHighlightFields().containsKey(NoteIndex.FIELD_NOTE_TITLE)) {
                    highlightedTitle = hit.getHighlightFields().get(NoteIndex.FIELD_NOTE_TITLE).fragments()[0].string();
                }

                // 构建 VO 实体类
                SearchNoteResVO searchNoteRspVO = SearchNoteResVO.builder()
                        .noteId(noteId)
                        .cover(cover)
                        .title(title)
                        .highlightTitle(highlightedTitle)
                        .avatar(avatar)
                        .nickname(nickname)
                        .updateTime(DateUtils.formatRelativeTime(updateTime))
                        .likeTotal(NumberUtils.formatNumberString(likeTotal))
                        .commentTotal(NumberUtils.formatNumberString(commentTotal))
                        .collectTotal(NumberUtils.formatNumberString(collectTotal))
                        .build();
                searchNoteResVOS.add(searchNoteRspVO);
            }
        }catch(Exception e) {
            log.error("==>查询 elasticsearch 异常", e);
        }
        return PageResponse.success(searchNoteResVOS, pageNo, total);
    }
}
