package com.cake.ai.repositoryServer.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cake.ai.entity.EmployeeChatHistory;
import com.cake.ai.mapper.AIMapper;
import com.cake.ai.repositoryServer.ChatHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
@Slf4j
@Service
public class InMemoryChatHistoryRepository implements ChatHistoryRepository {

    @Autowired
    private AIMapper aiMapper;

    /**
     * 保存员工ID&类型
     * @param type 业务类型，如：chat、service、pdf
     * @param Id 员工ID
     */
    @Override
    public void save(String type, String Id) {
        String existId = String.valueOf(getIdByType(type));
        if (existId == null) {
            EmployeeChatHistory employeeChatHistory = new EmployeeChatHistory();
            employeeChatHistory.setChatType(type);
            employeeChatHistory.setEmployeeId(Long.valueOf(Id));
            employeeChatHistory.setCreateTime(LocalDateTime.now());
            aiMapper.insert(employeeChatHistory);
        }else{
            log.info("该员工已有该类型的聊天记录,type:" + type + ",Id:" + Id);
        }

    }

    /**
     * 通过类型获取员工ID
     * @param type 业务类型
     * @return
     */
    @Override
    public Long getIdByType(String type) {
        LambdaQueryWrapper<EmployeeChatHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmployeeChatHistory::getChatType, type)
                .select(EmployeeChatHistory::getEmployeeId);
        EmployeeChatHistory employeeChatHistory = aiMapper.selectOne(wrapper);
        return employeeChatHistory !=null ?employeeChatHistory.getEmployeeId():null;
    }
}
