package com.cake.service.impl;

import com.cake.constant.MessageConstant;
import com.cake.constant.StatusConstant;
import com.cake.dto.DishDTO;
import com.cake.dto.DishPageQueryDTO;
import com.cake.entity.Dish;
import com.cake.entity.DishFlavor;
import com.cake.exception.DeletionNotAllowedException;
import com.cake.mapper.DishFlavorMapper;
import com.cake.mapper.DishMapper;
import com.cake.mapper.SetmealDishMapper;
import com.cake.result.PageResult;
import com.cake.service.DishService;
import com.cake.vo.DishVO;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增菜品和对应的口味
     * @param dishDTO
     */
    @Override
    @Transactional//事务管理注解，保证数据的一致性和完整性
    public void saveDishAndFlavor(DishDTO dishDTO) {

        Dish dish = new Dish();

        BeanUtils.copyProperties(dishDTO,dish);//拷贝属性，属性命名必须保持一致
        dish.setStatus(StatusConstant.DISABLE);//默认状态 禁止
        //向菜品表插入一条数据
        dishMapper.insert(dish);
        //获取insert语句生成的主键值dishId
        Long dishId=dish.getId();
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && flavors.size() > 0) {
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });//Lambda表达式，遍历菜品对应所有口味，把dishId设置到每个口味对象上
            //向口味表插入n条数据（带有dishId）
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    @Transactional
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        List<DishVO> records =page.getResult();
        return new PageResult(page.getTotal(),records);
    }

    /**
     * 菜品批量删除
     * @param ids
     */
    public void deleteBatch(List<Long> ids) {
        //判断当前菜品是否能删除--是否存在起售中的菜品?
        for(Long id:ids){
            Dish dish = dishMapper.getById(id);
            if(dish.getStatus().equals(StatusConstant.ENABLE)){
                //当前菜品处于 起售中 不能删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //判断当前菜品是否能删除--是否被套餐关联了?
        List<Long> setmealIds = setmealDishMapper.getSetmealDishIds(ids);
        if(setmealIds != null && setmealIds.size() > 0){
            //当前菜品被套餐关联了，不能删除
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        //删除菜品表中的菜品数据
        for (Long id : ids) {
            dishMapper.deleteById(id);
            //删除菜品关联的口味数据
            dishFlavorMapper.deleteByDishId(id);
        }
    }
}
