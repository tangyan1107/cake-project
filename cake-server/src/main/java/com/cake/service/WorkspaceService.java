package com.cake.service;


import com.cake.vo.BusinessDataVO;
import com.cake.vo.DishOverViewVO;
import com.cake.vo.OrderOverViewVO;
import com.cake.vo.SetmealOverViewVO;

public interface WorkspaceService {

    /**
     * 查询今日数据
     * @return
     */
    BusinessDataVO businessDate();

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
