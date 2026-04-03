package com.cake.ai.service.impl;

import com.cake.ai.entity.Category;
import com.cake.ai.mapper.CategoryMapper;
import com.cake.ai.service.ICategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 菜品及套餐分类 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-03-27
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {

}
