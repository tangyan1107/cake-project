package com.cake.service.impl;

import com.cake.dto.GoodsSalesDTO;
import com.cake.entity.Orders;
import com.cake.mapper.OrderMapper;
import com.cake.mapper.UserMapper;
import com.cake.service.ReportService;
import com.cake.vo.OrderReportVO;
import com.cake.vo.SalesTop10ReportVO;
import com.cake.vo.TurnoverReportVO;
import com.cake.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;

    /**
     * 统计指定时间区间内的营业额数据
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverReport(LocalDate begin, LocalDate end) {
        //当前集合用于存放从begin到end范围内的每天日期
        List<LocalDate> dateList = new ArrayList();

        dateList.add(begin);

        while(!begin.equals(end)){
            //日期计算，计算指定日期的后一天对应的日期
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        //存放每天的营业额
        List<Double> turnoverList = new ArrayList<>();
        for(LocalDate date:dateList){
            //查询date日期对应的营业额,营业额是指状态为“已完成”的订单金额合计
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            //select sum(amount) from orders where order_time > ? and order_time < ? and status = 5
            Map map =  new HashMap();
            map.put("beginTime",beginTime);
            map.put("endTime",endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }

        //封装返回结果
        return TurnoverReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))//用“,”分隔开
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    /**
     * 统计指定时间内的新增用户和总用户数据
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserReport(LocalDate begin, LocalDate end) {
        //存放从begin到end之间每天对应的日期
        List<LocalDate> dateList = new ArrayList();
        dateList.add(begin);
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        //存放每天的新增用户数量
        List<Integer> newUserList = new ArrayList<>();
        //存放每天总用户数量
        List<Integer> totalUserList = new ArrayList<>();

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map =  new HashMap();
            map.put("endTime",endTime);
            Integer totalUser = userMapper.countByMap(map);//总用户数量
            totalUserList.add(totalUser);

            map.put("beginTime", beginTime);
            Integer newUser = userMapper.countByMap(map);//新增用户数量
            newUserList.add(newUser);
        }

        return UserReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .build();
    }

    /**
     * 统计指定时间内的订单相关数据
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getStatisticsReport(LocalDate begin, LocalDate end) {
        //存放从begin到end之间每天对应的日期
        List<LocalDate> dateList = new ArrayList();
        dateList.add(begin);
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        //存放每天的订单数量列表
        List<Integer> orderCountList = new ArrayList<>();
        //存放每天有效订单数量列表
        List<Integer> validOrderCountList = new ArrayList<>();
        //存放订单总数
        Integer totalOrderCount =0;
        //存放有效订单总数
        Integer validOrderCount =0;
        //存放订单完成率
        Double orderCompletionRate = 0.0;

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map =  new HashMap();
            map.put("beginTime",beginTime);
            map.put("endTime",endTime);
            Integer totalOrder = orderMapper.countOrderByMap(map);//每日订单数量
            orderCountList.add(totalOrder);
            if(totalOrder!=null){
                totalOrderCount = totalOrderCount + totalOrder;
            }

            map.put("status", Orders.COMPLETED);
            Integer validOrder = orderMapper.countOrderByMap(map);//每日有效订单数
            validOrderCountList.add(validOrder);
            if(validOrder!=null){
                validOrderCount = validOrderCount + validOrder;
            }
        }
        orderCompletionRate = totalOrderCount==0 ? 0.0 :(validOrderCount*1.0/totalOrderCount);

        return OrderReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList,","))
                .validOrderCountList(StringUtils.join(validOrderCountList,","))
                .orderCompletionRate(orderCompletionRate)
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .build();

    }

    /**
     * 统计指定时间内的销量排名top10
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime, endTime);
        List<String> names = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String nameList = StringUtils.join(names, ",");
        List<Integer> numbers = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String numberList = StringUtils.join(numbers, ",");

        return SalesTop10ReportVO
                .builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }


}
