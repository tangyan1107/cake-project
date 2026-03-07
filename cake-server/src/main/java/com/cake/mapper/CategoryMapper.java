package com.cake.mapper;

import com.cake.dto.CategoryPageQueryDTO;
import com.cake.entity.Category;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper {

    /**
     * 根据类型查询分类
     * @param type
     * @return
     */
    @Select("select * from category where type = #{type}")
    List<Category> getByType(Integer type);

    /**
     * 根据id修改/更新分类
     * @param category
     */
    void update(Category category);

    /**
     * 分页查询
     * @param caregoryPageQueryDTO
     * @return
     */
    Page<Category> pageQuery(CategoryPageQueryDTO caregoryPageQueryDTO);

    /**
     * 新增分类
     * @param category
     */
    @Insert("insert into category(type,name,sort,status,create_time,update_time,create_user, update_user)" +
            "values" +
            "(#{type},#{name},#{sort},#{status},#{createTime},#{updateTime},#{createUser},#{updateUser})")
    void insert(Category category);

    /**
     * 根据id删除分类
     * @param id
     */
    @Delete("delete from category where id = #{id}")
    void delete(Long id);

    /**
     * 根据id查询分类
     * @param id
     * @return
     */
    @Select("select * from category where id = #{id}")
    Category getById(Long id);
}
