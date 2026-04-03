package com.cake.ai.entity.query;

import lombok.Data;
import org.springframework.ai.tool.annotation.ToolParam;

@Data
public class CategoryQuery {
    @ToolParam(required = false,description = "分类名称：面包,甜品,干点,司康,饮品,配件,人气套餐,今日特卖套餐")
    private String name;
    @ToolParam(required = false,description = "分类id：11,12,16,17,18,19,31,32,33")
    private Long id;
    @ToolParam(required = false,description = "分类类型：1-菜品，2-套餐")
    private Integer type;
    @ToolParam(required = false,description = "分类状态：1-起售，0-停售")
    private Integer status;
}
