package com.cake.controller.admin;


import com.cake.dto.CategoryDTO;
import com.cake.dto.CategoryPageQueryDTO;
import com.cake.entity.Category;
import com.cake.result.PageResult;
import com.cake.result.Result;
import com.cake.service.CategoryService;
import com.cake.service.EmployeeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/category")
@Slf4j
@Api(tags = "分类相关接口")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 分类分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分类分页查询")
    public Result<PageResult> page (CategoryPageQueryDTO categoryPageQueryDTO){
        log.info("分页查询：#{}", categoryPageQueryDTO);
        PageResult pageResult =categoryService.pageQuery(categoryPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 新增分类
     * @param categoryDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增分类")
    public Result addCategory(@RequestBody CategoryDTO categoryDTO){
        log.info("新增分类：#{}", categoryDTO);
        categoryService.addCategory(categoryDTO);
        return Result.success();
    }

    /**
     * 启用禁用分类
     * @param status
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("启用、禁用分类")
    public Result startOrStop(@PathVariable Integer status,Long id){
        log.info("启用、禁用分类：#{}", status);
        categoryService.startOrStop(status,id);
        return Result.success();
    }

    /**
     * 编辑分类信息
     * @param categoryDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改分类信息")
    public Result edit (@RequestBody CategoryDTO categoryDTO){
        log.info("修改分类信息：{}", categoryDTO);
        categoryService.edit(categoryDTO);
        return Result.success();
    }

    /**
     * 根据id删除分类
     * @param id
     * @return
     */
    @DeleteMapping
    @ApiOperation("根据id删除分类")
    public Result delete(Long id){
        log.info("删除分类：{}",id);
        categoryService.delete(id);
        return Result.success();
    }

    /**
     * 根据类型查询分类
     * @param type
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据类型查询分类")
    public Result<List<Category>> getByType(Integer type){
        log.info("根据类型查询分类：{}",type);
        List<Category> list = categoryService.getByType(type);
        return Result.success(list);
    }

    /**
     * 根据id查询分类
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询分类")
    public Result<Category> getById(@PathVariable Long id){
        log.info("根据id查询分类：{}",id);
        Category category = categoryService.getById(id);
        return Result.success(category);
    }
}
