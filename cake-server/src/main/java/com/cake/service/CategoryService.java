package com.cake.service;

import com.cake.dto.CategoryDTO;
import com.cake.dto.CategoryPageQueryDTO;
import com.cake.entity.Category;
import com.cake.result.PageResult;

import java.util.List;

public interface CategoryService {

    /**
     * 新增分类
     * @param categoryDTO
     */
    void addCategory(CategoryDTO categoryDTO);

    /**
     * 分类分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

    /**
     * 启用、禁用分类
     * @param status
     */
    void startOrStop(Integer status,Long id);

    /**
     * 编辑分类
     * @param categoryDTO
     */
    void edit(CategoryDTO categoryDTO);

    /**
     * 根据id删除分类
     * @param id
     */
    void delete(Long id);

    /**
     * 根据类型查询分类
     * @return
     */
    List<Category> getByType(Integer type);

    /**
     * 根据id查询分类
     * @param id
     * @return
     */
    Category getById(Long id);
}
