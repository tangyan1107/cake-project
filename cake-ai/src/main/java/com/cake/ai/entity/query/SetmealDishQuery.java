package com.cake.ai.entity.query;

import lombok.Data;
import org.springframework.ai.tool.annotation.ToolParam;

@Data
public class SetmealDishQuery {
    @ToolParam(required = false,description = "根据套餐id查询菜品id")
    private Long setmealId;
}
