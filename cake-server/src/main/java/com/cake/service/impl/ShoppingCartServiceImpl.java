package com.cake.service.impl;

import com.cake.context.BaseContext;
import com.cake.dto.ShoppingCartDTO;
import com.cake.entity.Dish;
import com.cake.entity.Setmeal;
import com.cake.entity.ShoppingCart;
import com.cake.mapper.DishMapper;
import com.cake.mapper.SetmealMapper;
import com.cake.mapper.ShoppingCartMapper;
import com.cake.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        //判断当前加入购物车中的商品是否已经存在了
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        //如果已经存在了，需要数量+1
        if(list != null && list.size()>0){
            ShoppingCart cart = list.get(0);
            cart.setNumber(cart.getNumber()+1);//update shopping_cart set number = ? where id = ?
            shoppingCartMapper.updateNumberById(cart);
        }else{
            //如果不存在，需要插入购物车数据
            //判断本次添加到购物车的是菜品还是套餐
            Long dishId = shoppingCartDTO.getDishId();
            if(dishId != null){
                //本次添加到购物车的是菜品
                Dish dish = dishMapper.getById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());

            }else{
                //本次添加到购物车的是套餐
                Long setmealId = shoppingCartDTO.getSetmealId();
                Setmeal setmeal = setmealMapper.getById(setmealId);
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());

            }
            //统一插入数据
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);

        }
    }

    /**
     * 查看购物车
     * @return
     */
    @Override
    public List<ShoppingCart> showShoppingCart() {
        //获取到当前用户id
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(userId).build();
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        return list;
    }

    /**
     * 清空购物车
     */
    @Override
    public void clean() {
        //获取到当前用户id
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.clean(userId);
    }

    /**
     * 删除购物车中一个商品
     * @param shoppingCartDTO
     */
    @Override
    public void subShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        //查询当前用户的购物车数据，判断number
        shoppingCart.setUserId(BaseContext.getCurrentId());
        log.info("查询购物车参数：userId={},dishId={},dishFlavor={}",
                shoppingCart.getUserId(), shoppingCart.getDishId(), shoppingCart.getDishFlavor());
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if(list != null && list.size()>0){
            log.info("查询到{}条记录,第一条记录id:{},dishId:{},dishFlavor:{}",
                    list.size(), list.get(0).getId(), list.get(0).getDishId(), list.get(0).getDishFlavor());
            shoppingCart = list.get(0);

            Integer number = shoppingCart.getNumber();
            if(number == 1){
                //直接删除数据
                shoppingCartMapper.delete(shoppingCart.getId());
            }else{
                //数量大于1时，仅修改数量
                shoppingCart.setNumber(shoppingCart.getNumber() - 1);
                shoppingCartMapper.updateNumberById(shoppingCart);
            }

        }
    }


}
