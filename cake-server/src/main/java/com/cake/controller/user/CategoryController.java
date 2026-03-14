package com.cake.controller.user;

import com.cake.entity.Category;
import com.cake.result.Result;
import com.cake.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userCategoryController")
@Slf4j
@Api(tags = "C端-分类接口")
@RequestMapping("/user/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 分类查询
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("C端-查询分类")
    public Result<List<Category>> list(Integer type){
        log.info("C端-查询分类:{}",type);
        List<Category> list = categoryService.getByType(type);
        return Result.success(list);
    }
}
