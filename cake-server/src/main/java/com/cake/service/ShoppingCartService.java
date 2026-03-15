package com.cake.service;

import com.cake.dto.ShoppingCartDTO;
import com.cake.entity.ShoppingCart;

import java.util.List;


public interface ShoppingCartService {

    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    void addShoppingCart(ShoppingCartDTO shoppingCartDTO);

    /**
     * 查看购物车
     * @return
     */
    List<ShoppingCart> showShoppingCart();

    /**
     * 清空购物车
     */
    void clean();

    /**
     * 删除购物车直播个一个商品
     * @param shoppingCartDTO
     */
    void subShoppingCart(ShoppingCartDTO shoppingCartDTO);
}
