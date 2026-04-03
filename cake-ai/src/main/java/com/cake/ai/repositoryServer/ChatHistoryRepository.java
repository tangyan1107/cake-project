package com.cake.ai.repositoryServer;


public interface ChatHistoryRepository {

    /**
     * 保存会话记录
     * @param type 业务类型，如：chat、service、pdf
     * @param Id 员工ID
     */
    void save(String type, String Id);

    /**
     * 获取历史员工ID列表
     * @param type 业务类型
     * @return 历史员工id列表
     */
    Long getIdByType(String type);
}
