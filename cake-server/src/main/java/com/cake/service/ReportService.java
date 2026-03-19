package com.cake.service;

import com.cake.vo.OrderReportVO;
import com.cake.vo.SalesTop10ReportVO;
import com.cake.vo.TurnoverReportVO;
import com.cake.vo.UserReportVO;

import java.time.LocalDate;

public interface ReportService {

    /**
     * 统计指定时间区间内的营业额数据
     * @param begin
     * @param end
     * @return
     */
    TurnoverReportVO getTurnoverReport(LocalDate begin, LocalDate end);

    /**
     * 统计指定时间内的新增用户和总用户数据
     * @param begin
     * @param end
     * @return
     */
    UserReportVO getUserReport(LocalDate begin, LocalDate end);

    /**
     * 统计指定时间内的订单相关数据
     * @param begin
     * @param end
     * @return
     */
    OrderReportVO getStatisticsReport(LocalDate begin, LocalDate end);

    /**
     * 统计指定时间内的销量排名top10的商品
     * @param begin
     * @param end
     * @return
     */
    SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end);
}
