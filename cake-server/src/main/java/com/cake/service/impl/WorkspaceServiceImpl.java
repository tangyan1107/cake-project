package com.cake.service.impl;

import com.cake.entity.Orders;
import com.cake.mapper.DishMapper;
import com.cake.mapper.OrderMapper;
import com.cake.mapper.SetmealMapper;
import com.cake.mapper.UserMapper;
import com.cake.service.OrderService;
import com.cake.service.ReportService;
import com.cake.service.WorkspaceService;
import com.cake.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class WorkspaceServiceImpl implements WorkspaceService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ReportService reportService;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 根据传入日期查询数据
     * @return
     */
    @Override
    public BusinessDataVO businessDate(LocalDateTime begin,LocalDateTime end) {
        /*LocalDate date = LocalDate.now();
        Map map = mapTime();
        OrderReportVO orderReportVO = reportService.getStatisticsReport(date,date);
        Double orderCompletionRate = orderReportVO.getOrderCompletionRate();//订单完成率*/
        Map map = new HashMap();
        map.put("beginTime",begin);
        map.put("endTime",end);

        //查询总订单数
        Integer totalOrderCount = orderMapper.countOrderByMap(map);

        map.put("status",Orders.COMPLETED);
        Double turnover = orderMapper.sumByMap(map);
        turnover = turnover == null ? 0.0 : turnover;//营业额

        Integer validOrder = orderMapper.countOrderByMap(map);//每日有效订单数

        Double unitPrice;
        Double orderCompletion = 0.0;
        if( validOrder == null || validOrder == 0 ){
            unitPrice = 0.0;
        }else{
            unitPrice = turnover/validOrder;//平均客单价
            orderCompletion = validOrder.doubleValue()/totalOrderCount;//订单完成率
        }
        BusinessDataVO  businessDataVO = BusinessDataVO
                .builder()
                .newUsers(userMapper.countByMap(map))//新增用户数
                .orderCompletionRate(orderCompletion)
                .turnover(turnover)
                .validOrderCount(validOrder)
                .unitPrice(unitPrice)
                .build();
        return businessDataVO;
    }

    /**
     * 查询订单数据
     * @return
     */
    @Override
    public OrderOverViewVO overviewOrders() {
        Map map = mapTime();
        Integer allOrders = orderMapper.countOrderByMap(map);//获取全部订单
        map.put("status", Orders.TO_BE_CONFIRMED);
        Integer waitingOrders = orderMapper.countOrderByMap(map);//获取待接单数量
        map.put("status",Orders.CONFIRMED);
        Integer completedOrders = orderMapper.countOrderByMap(map);//获取待派送数量
        map.put("status",Orders.COMPLETED);
        Integer completeOrders = orderMapper.countOrderByMap(map);//获取已完成数量
        map.put("status",Orders.CANCELLED);
        Integer cancelledOrders = orderMapper.countOrderByMap(map);//获取已取消数量
        OrderOverViewVO orderOverViewVO = OrderOverViewVO
                .builder()
                .waitingOrders(waitingOrders)
                .deliveredOrders(completedOrders)
                .completedOrders(completeOrders)
                .cancelledOrders(cancelledOrders)
                .allOrders(allOrders)
                .build();
        return orderOverViewVO;
    }

    /**
     * 查询菜品总览
     * @return
     */
    @Override
    public DishOverViewVO overviewDishes() {
        DishOverViewVO dishOverViewVO = DishOverViewVO
                .builder()
                .discontinued(dishMapper.countDishByStatus(0))//获得已停售数量
                .sold(dishMapper.countDishByStatus(1))//获得已起售数量
                .build();
        return dishOverViewVO;
    }

    /**
     * 查询套餐数据
     * @return
     */
    @Override
    public SetmealOverViewVO overviewSetmeals() {
        SetmealOverViewVO setmealOverViewVO = SetmealOverViewVO
                .builder()
                .discontinued(setmealMapper.countSetmealByStatus(0))//获得停售数量
                .sold(setmealMapper.countSetmealByStatus(1))//获得起售数量
                .build();
        return setmealOverViewVO;
    }


    /**
     * mapTime今日
     * @return
     */
    private Map mapTime(){
        Map map = new HashMap();
        LocalDate date = LocalDate.now();
        LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
        map.put("beginTime", beginTime);
        map.put("endTime", endTime);
        return map;
    }
}
