package com.cake.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("employee_chat_history")
public class EmployeeChatHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("employee_id")
    private Long employeeId;
    @TableField("chat_type")
    private String chatType;
    @TableField("create_time")
    private LocalDateTime createTime;

}
