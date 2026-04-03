package com.cake.ai.entity.query;

import lombok.Data;
import org.springframework.ai.tool.annotation.ToolParam;

@Data
public class TurnoverReportQuery {
    @ToolParam(required = false,description = "日期，以逗号分隔，例如：2025-10-01,2025-10-02")
    private String dateList;
    @ToolParam(required = false,description = "查询开始日期，格式：yyyy-MM-dd，优先级高于dateList")
    private String beginDate;
    @ToolParam(required = false,description = "查询结束日期，格式：yyyy-MM-dd，优先级高于dateList")
    private String endDate;
}
