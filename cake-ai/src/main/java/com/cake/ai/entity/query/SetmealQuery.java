package com.cake.ai.entity.query;

import lombok.Data;
import org.springframework.ai.tool.annotation.ToolParam;

@Data
public class SetmealQuery {
    @ToolParam(required = false,description = "根据套餐名称查询套餐")
    private String name;
    @ToolParam(required = false,description = "根据套餐id查询菜品")
    private Long id;
    @ToolParam(required = false,description = "根据分类id查询套餐")
    private Long categoryId;
    @ToolParam(required = false,description = "套餐状态：1-起售，0-停售")
    private Integer status;
}
