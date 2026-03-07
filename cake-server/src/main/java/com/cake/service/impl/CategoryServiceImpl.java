package com.cake.service.impl;

import com.cake.constant.MessageConstant;
import com.cake.constant.StatusConstant;
import com.cake.context.BaseContext;
import com.cake.dto.CategoryDTO;
import com.cake.dto.CategoryPageQueryDTO;
import com.cake.entity.Category;
import com.cake.exception.DeletionNotAllowedException;
import com.cake.mapper.CategoryMapper;
import com.cake.mapper.DishMapper;
import com.cake.mapper.SetmealMapper;
import com.cake.result.PageResult;
import com.cake.service.CategoryService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class CategoryServiceImpl implements CategoryService {

@Autowired
private CategoryMapper categoryMapper;
@Autowired
private DishMapper dishMapper;
@Autowired
private SetmealMapper setmealMapper;

    /**
     * 新增分类
     * @param categoryDTO
     */
    @Override
    public void addCategory(CategoryDTO categoryDTO) {
        Category category = new Category();
        //对象属性拷贝，将categoryDTO里的属性拷贝给category(前提：属性名一致)
        BeanUtils.copyProperties(categoryDTO, category);
        //设置帐号状态，默认禁用状态
        category.setStatus(StatusConstant.DISABLE);
        //设置当前记录的创建时间和修改时间
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        //设置当前记录创建人id和修改人id
        category.setCreateUser(BaseContext.getCurrentId());
        category.setUpdateUser(BaseContext.getCurrentId());

        categoryMapper.insert(category);
    }

    /**
     * 分类分页查询
     * @param caregoryPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(CategoryPageQueryDTO caregoryPageQueryDTO) {
       PageHelper.startPage(caregoryPageQueryDTO.getPage(),caregoryPageQueryDTO.getPageSize());
        //从分页结果page中提取当前页的所有Employee员工记录，并赋值
       Page<Category> page = categoryMapper.pageQuery(caregoryPageQueryDTO);
       List<Category> records = page.getResult();
        //page.getTotal()调取符合查询条件的总记录数,records当前页码数据
        return new PageResult(page.getTotal(),records);
    }

    /**
     * 启用、禁用分类
     * @param status
     */
    @Override
    public void startOrStop(Integer status,Long id) {
        Category category = new Category();
        category.setStatus(status);
        category.setId(id);
        category.setUpdateTime(LocalDateTime.now());
        category.setUpdateUser(BaseContext.getCurrentId());
        categoryMapper.update(category);

    }

    /**
     * 修改分类
     * @param categoryDTO
     */
    @Override
    public void edit(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);//对对象属性拷贝
        category.setUpdateTime(LocalDateTime.now());
        category.setUpdateUser(BaseContext.getCurrentId());
        categoryMapper.update(category);
    }

    /**
     * 根据id删除分类
     * @param id
     */
    @Override
    public void delete(Long id) {
        //查询当前分类是否关联了菜品，如果关联了就抛出业务异常
        Integer count = dishMapper.countByCategoryId(id);
        if (count > 0) {
            //当前分类下有菜品，不能删除
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_DISH);
        }
        //查询当前分类是否关联了套餐，如果关联了就抛出业务异常
        count = setmealMapper.countByCategoryId(id);
        if (count > 0) {
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
        }

        //删除分类数据
        categoryMapper.delete(id);
    }

    /**
     * 根据类型查询分类
     * @return type
     */
    @Override
    public List<Category> getByType(Integer type) {
        return categoryMapper.getByType(type);
    }

    /**
     * 根据id查询分类
     * @param id
     * @return
     */
    @Override
    public Category getById(Long id) {
        return categoryMapper.getById(id);
    }


}
