package com.cake.controller.admin;


import com.cake.result.Result;
import com.cake.service.ReportService;
import com.cake.service.WorkspaceService;
import com.cake.vo.BusinessDataVO;
import com.cake.vo.DishOverViewVO;
import com.cake.vo.OrderOverViewVO;
import com.cake.vo.SetmealOverViewVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController
@RequestMapping("/admin/workspace")
@Slf4j
@Api(tags ="工作台相关接口")
public class WorkspaceController {

    @Autowired
    private WorkspaceService workspaceService;

    /**
     * 查询今日数据
     * @return
     */
    @GetMapping("/businessData")
    @ApiOperation("查询今日数据")
    public Result<BusinessDataVO> businessData(){
        log.info("查询今日数据");
        //获得当天开始时间
        LocalDateTime begin = LocalDateTime.now().with(LocalTime.MIN);
        //获得当天结束时间
        LocalDateTime end = LocalDateTime.now().with(LocalTime.MAX);
        BusinessDataVO businessDataVO = workspaceService.businessDate(begin,end);
        return Result.success(businessDataVO);
    }


    /**
     * 查询订单数据
     * @return
     */
    @GetMapping("/overviewOrders")
    @ApiOperation("查询订单数据")
    public Result<OrderOverViewVO>  overviewOrders(){
        log.info("查询订单数据");
        OrderOverViewVO orderOverViewVO = workspaceService.overviewOrders();
        return Result.success(orderOverViewVO);
    }

    /**
     * 查询菜品数据
     * @return
     */
    @GetMapping("/overviewDishes")
    @ApiOperation("查询菜品数据")
    public Result<DishOverViewVO> overviewDishes(){
        log.info("查询菜品数据");
        DishOverViewVO dishOverViewVO = workspaceService.overviewDishes();
        return Result.success(dishOverViewVO);
    }

    /**
     * 查询套餐数据
     * @return
     */
    @GetMapping("/overviewSetmeals")
    @ApiOperation("查询套餐数据")
    public Result<SetmealOverViewVO> overviewSetmeals(){
        log.info("查询套餐数据");
        SetmealOverViewVO setmealOverViewVO = workspaceService.overviewSetmeals();
        return Result.success(setmealOverViewVO);
    }
}
