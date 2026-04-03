package com.cake.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cake.ai.entity.EmployeeChatHistory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AIMapper extends BaseMapper<EmployeeChatHistory> {
}
