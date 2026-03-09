package com.cake.service.impl;

import com.cake.dto.DishDTO;
import com.cake.entity.Dish;
import com.cake.entity.DishFlavor;
import com.cake.mapper.DishFlavorMapper;
import com.cake.mapper.DishMapper;
import com.cake.service.DishService;
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

    /**
     * 新增菜品和对应的口味
     * @param dishDTO
     */
    @Override
    @Transactional//事务管理注解，保证数据的一致性和完整性
    public void saveDishAndFlavor(DishDTO dishDTO) {

        Dish dish = new Dish();

        BeanUtils.copyProperties(dishDTO,dish);//拷贝属性，属性命名必须保持一致

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
}
