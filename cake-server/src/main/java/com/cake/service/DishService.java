package com.cake.service;

import com.cake.dto.DishDTO;

public interface DishService {

    /**
     * 新增菜品和对应的口味
     * @param dishDTO
     */
    public void saveDishAndFlavor(DishDTO dishDTO);
}
