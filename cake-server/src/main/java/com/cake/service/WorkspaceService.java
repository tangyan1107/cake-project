package com.cake.service;


import com.cake.vo.BusinessDataVO;
import com.cake.vo.DishOverViewVO;
import com.cake.vo.OrderOverViewVO;
import com.cake.vo.SetmealOverViewVO;

import java.time.LocalDateTime;

public interface WorkspaceService {

    /**
     * 根据时间段统计营业时间
     * @return
     */
    BusinessDataVO businessDate(LocalDateTime begin, LocalDateTime end);

    /**
     * 查询订单数据
     * @return
     */
    OrderOverViewVO overviewOrders();

    /**
     * 查询菜品总览
     * @return
     */
    DishOverViewVO overviewDishes();

    /**
     * 查询套餐数据
     * @return
     */
    SetmealOverViewVO overviewSetmeals();
}
