package com.cake.ai.tools;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.cake.ai.entity.*;
import com.cake.ai.entity.query.*;
import com.cake.ai.mapper.OrdersMapper;
import com.cake.ai.service.ICategoryService;
import com.cake.ai.service.IDishService;
import com.cake.ai.service.ISetmealDishService;
import com.cake.ai.service.ISetmealService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Component
public class Tools {

    private final IDishService dishService;
    private final ICategoryService categoryService;
    private final ISetmealDishService setmealDishService;
    private final ISetmealService setmealService;
    private final OrdersMapper ordersMapper;

    @Tool(description = "根据条件查询菜品",returnDirect = false)
    public List<Dish> queryDish(@ToolParam(description = "查询菜品的条件") DishQuery dishQuery) {
        if(dishQuery == null){
            return dishService.list();
        }
        QueryChainWrapper<com.cake.ai.entity.Dish> wrapper = dishService.query()
                .eq(dishQuery.getName() != null,"name",dishQuery.getName())
                .eq(dishQuery.getId() != null,"id", dishQuery.getId())
                .eq(dishQuery.getCategoryId() != null, "CategoryId", dishQuery.getCategoryId())
                .eq(dishQuery.getStatus() != null, "status", dishQuery.getStatus());

        return wrapper.list();
    }

    @Tool(description = "根据条件查询分类",returnDirect = false)
    public List<Category> queryCategory(@ToolParam(description = "查询分类条件")CategoryQuery categoryQuery) {
        if(categoryQuery == null){
            return categoryService.list();
        }
        QueryChainWrapper<com.cake.ai.entity.Category> wrapper = categoryService.query()
                .eq(categoryQuery.getName() != null,"name", categoryQuery.getName())
                .eq(categoryQuery.getId() != null,"id", categoryQuery.getId())
                .eq(categoryQuery.getType() != null, "type", categoryQuery.getType())
                .eq(categoryQuery.getStatus() != null, "status", categoryQuery.getStatus());
        return wrapper.list();
    }

    @Tool(description = "根据条件查询套餐分类",returnDirect = false)
    public List<Setmeal> querySetmeal(@ToolParam(description = "查询套餐条件") SetmealQuery setmealQuery) {
        if(setmealQuery == null){
            return setmealService.list();
        }
        QueryChainWrapper<com.cake.ai.entity.Setmeal> wrapper = setmealService.query()
                .eq(setmealQuery.getName() != null,"name",setmealQuery.getName())
                .eq(setmealQuery.getId() != null,"id", setmealQuery.getId())
                .eq(setmealQuery.getCategoryId() != null, "categoryId", setmealQuery.getCategoryId())
                .eq(setmealQuery.getStatus() != null, "status", setmealQuery.getStatus());
        return wrapper.list();
    }
    @Tool(description = "根据套餐id查询套餐内具体菜品",returnDirect = false)
    public List<Dish> querySetmealDish(@ToolParam(description = "根据套餐id查询该套餐内具体菜品条件")SetmealDishQuery setmealDishQuery) {
        if(setmealDishQuery == null){
            return List.of();
        }
        //根据套餐id查询对象列表
        List<SetmealDish> setmealDishList = setmealDishService.query()
                .eq(setmealDishQuery.getSetmealId()!=null,"setmeal_id",setmealDishQuery.getSetmealId()).list();
        //提取菜品id
        List<Long> dishIds = setmealDishList.stream()
                .map(SetmealDish::getDishId)
                .toList();
        //用菜品id查询对应的菜品信息
        if(dishIds.isEmpty()){
            return Collections.emptyList();
        }
            //批量查询
        List<Dish> dishList = dishService.query()
                .in("id",dishIds)
                .list();
        return dishList;
    }

    @Tool(description = "查询时间段内的营业额数据,支持逗号分隔日期列表或区间")
    public List<TurnoverReport> queryTurnoverReport(@ToolParam(description = "营业额数据条件，包含日期列表或日期区间")TurnoverReportQuery  turnoverReportQuery) {
        //1.校验参数
        if(turnoverReportQuery == null){
            return List.of();
        }
        //2.解析日期
        List<LocalDate> dateList = new ArrayList();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            //优先使用日期，生成需要查询的日期列表
        if(StringUtils.isNotBlank(turnoverReportQuery.getBeginDate())&&StringUtils.isNotBlank(turnoverReportQuery.getEndDate())){
            LocalDate end = LocalDate.parse(turnoverReportQuery.getEndDate(), formatter);
            LocalDate begin = LocalDate.parse(turnoverReportQuery.getBeginDate(), formatter);
            //生成区间内所有日期
            dateList.add(begin);
            while(!begin.equals(end)){
                begin = begin.plusDays(1);
                dateList.add(begin);
            }
        }
            //用逗号分隔的日期列表
        else if(StringUtils.isNotBlank(turnoverReportQuery.getDateList())){
            dateList = Arrays.stream(turnoverReportQuery.getDateList().split(","))
                    .map(String::trim)
                    .map(dateStr -> LocalDate.parse(dateStr,formatter))
                    .toList();
        }else{
            //无日期参数，返回空
            return List.of();
        }
        //3.按天查询营业额
        List<TurnoverReport> result = new ArrayList<>();
        for(LocalDate date : dateList){
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
            wrapper.between(Orders::getOrderTime, beginTime, endTime)
                    .eq(Orders::getStatus,5)
                    .select(Orders::getAmount);
            //计算当天营业额
            Double turnover = ordersMapper.selectObjs(wrapper)
                    .stream()
                    .mapToDouble(obj -> obj == null ? 0.0 : Double.parseDouble(obj.toString()))
                    .sum();
            //封装返回实体
            TurnoverReport report = new TurnoverReport();
            report.setDateList(date.format(formatter));
            report.setTurnoverList(String.valueOf(turnover == null ? 0.0 :turnover));
            result.add(report);
        }
        return result;
    }
}
