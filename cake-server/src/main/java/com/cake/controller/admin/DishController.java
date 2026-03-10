package com.cake.controller.admin;


import com.cake.dto.DishDTO;
import com.cake.dto.DishPageQueryDTO;
import com.cake.entity.Dish;
import com.cake.result.PageResult;
import com.cake.result.Result;
import com.cake.service.DishService;
import com.cake.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜品管理
 */
@RestController
@RequestMapping("/admin/dish")
@Slf4j
@Api(tags = "菜品相关接口")
public class DishController {

    @Autowired
    private DishService dishService;
    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result saveDish(@RequestBody DishDTO dishDTO) {
       log.info("新增菜品:{}", dishDTO);
       dishService.saveDishAndFlavor(dishDTO);
        return Result.success();
    }

    /**
     * 菜品分页查询
     * @param
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> page (DishPageQueryDTO dishPageQueryDTO) {
        log.info("菜品分页查询，参数为:{}",dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 菜品的批量删除
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("批量删除菜品")
    public Result deleteDish(@RequestParam List<Long> ids) {
        log.info("菜品批量删除：{}",ids);
        dishService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 根据id查询菜品及对应口味
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品及对应口味")
    public Result<DishVO> getDishById(@PathVariable Long id) {
        log.info("根据id查询菜品及口味:{}",id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    /**
     * 修改菜品信息
     * @param dishDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改菜品信息")
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品信息:{}",dishDTO);
        dishService.updateWithFlavor(dishDTO);
        return Result.success();
    }

    /**
     * 启用禁用菜品
     * @param status
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("启用、禁用菜品")
    public Result startOrStop(@PathVariable Integer status,Long id){
        log.info("启用、禁用菜品：#{}", status);
        dishService.startOrStop(status,id);
        return Result.success();
    }
}
