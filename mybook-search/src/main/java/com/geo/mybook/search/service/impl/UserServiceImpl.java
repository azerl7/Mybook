package com.geo.mybook.search.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import com.geo.framework.common.response.PageResponse;
import com.geo.framework.common.util.NumberUtils;
import com.geo.mybook.search.domain.vo.SearchUserReqVO;
import com.geo.mybook.search.domain.vo.SearchUserResVO;
import com.geo.mybook.search.index.UserIndex;
import com.geo.mybook.search.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/*
creator：AZERL7
createTime：15:48
*/
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private RestHighLevelClient restHighLevelClient;

    @Override
    public PageResponse<SearchUserResVO> searchUser(SearchUserReqVO searchUserReqVO) {

        //1、获取查询信息
        String keyword = searchUserReqVO.getKeyword();
        Integer pageNo = searchUserReqVO.getPageNo();

        //2、构建请求

//        GET /user/_search
//        {
//            "query": {
//              "multi_match": {
//                  "query": "azerl7",
//                  "fields": ["nickname", "mybook_id"]
//              }
//            },
//            "sort": [
//            {
//                "fans_total": {
//                   "order": "desc"
//                 }
//            }
//            ],
//            "from": 0,
//            "size": 10
//        }

        SearchRequest searchRequest = new SearchRequest(UserIndex.NAME);

        SearchSourceBuilder sourceBuilder  = new SearchSourceBuilder();

        //2.1、构建 multi_match 查询，查询 nickname 和 mybook_id 字段
        sourceBuilder.query(QueryBuilders.multiMatchQuery(
                keyword, UserIndex.FIELD_USER_NICKNAME, UserIndex.FIELD_USER_MYBOOK_ID));
        SortBuilder<?> sortBuilder = SortBuilders.fieldSort(UserIndex.FIELD_USER_FANS_TOTAL);

        //2.1、高亮字段
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field(UserIndex.FIELD_USER_NICKNAME)
                .preTags("<strong>")
                .postTags("</strong>");

        //2.3、构建
        sourceBuilder.highlighter(highlightBuilder);
        sourceBuilder.sort(sortBuilder);

        //2.4、将构建的查询条件设置到 SearchRequest 中
        searchRequest.source(sourceBuilder);

        //3、设置分页，from和size
        int pageSize=10;
        int from=(pageNo-1)*pageSize;

        List<SearchUserResVO> searchUserResVOS = null;
        long total=0;
        try{
            log.info("==> SearchRequest: {}",searchRequest);
            //4、执行查询请求
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            total=searchResponse.getHits().getTotalHits().value;
            log.info("==> 命中文档数目, hits:{}",total);
            searchUserResVOS= Lists.newArrayList();

            //5、获取搜索命中的文档列表
            for(SearchHit hit : searchResponse.getHits().getHits()) {
                Map<String,Object> sourceMap=hit.getSourceAsMap();
                Long userId = ((Number) sourceMap.get(UserIndex.FIELD_USER_ID)).longValue();
                String nickname = (String) sourceMap.get(UserIndex.FIELD_USER_NICKNAME);
                String avatar = (String) sourceMap.get(UserIndex.FIELD_USER_AVATAR);
                String mybookId = (String) sourceMap.get(UserIndex.FIELD_USER_MYBOOK_ID);
                Integer noteTotal = (Integer) sourceMap.get(UserIndex.FIELD_USER_NOTE_TOTAL);
                Integer fansTotal = (Integer) sourceMap.get(UserIndex.FIELD_USER_FANS_TOTAL);
                String highlightedNickname=null;
                if(CollectionUtil.isNotEmpty(hit.getHighlightFields())
                        && hit.getHighlightFields().containsKey(UserIndex.FIELD_USER_NICKNAME)) {
                    highlightedNickname = hit.getHighlightFields().get(UserIndex.FIELD_USER_NICKNAME).fragments()[0].string();
                }
                //构建vo
                SearchUserResVO searchUserResVO = SearchUserResVO.builder()
                        .userId(userId)
                        .nickName(nickname)
                        .avatar(avatar)
                        .mybookId(mybookId)
                        .noteTotal(noteTotal)
                        .fansTotal(NumberUtils.formatNumberString(fansTotal))
                        .highlightNickname(highlightedNickname)
                        .build();
                searchUserResVOS.add(searchUserResVO);
            }
        }catch(Exception e){
            log.error("==> 查询 elasticsearch 异常",e);
        }
        return PageResponse.success(searchUserResVOS,pageNo,total);
    }
}
