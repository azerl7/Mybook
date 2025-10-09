package com.geo.mybook.user.dto.req;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/*
creator：AZERL7
createTime：17:34
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FindUsersByIdsReqDTO {
    @NotNull(message="用户id集合不能为空")
    @Size(min=1,max=10,message="用户id集合大小不能小于1最多不能超过10")
    private List<Long> ids;
}
