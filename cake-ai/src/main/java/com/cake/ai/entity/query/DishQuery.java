package com.cake.ai.entity.query;

import lombok.Data;
import org.springframework.ai.tool.annotation.ToolParam;

@Data
public class DishQuery {
    @ToolParam(required = false,description = "菜品名称：蒜香法棍,蝴蝶酥,半熟芝士 - 两个装,芋泥半熟芝士 - 两个装,抹茶千层切角,薄荷柠檬气泡水,蛋糕配件")
    private String name;
    @ToolParam(required = false,description = "菜品id：72,74,75,76,77,78,79,80")
    private Long id;
    @ToolParam(required = false,description = "菜品状态：1-起售，0-停售")
    private Integer status;
    @ToolParam(required = false,description = "分类id：31,19,12,12,11,16,12,18")
    private Long categoryId;
}
