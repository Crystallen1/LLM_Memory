package org.crystallen.lc.service;

import java.util.List;
import java.util.Map;

public interface VectorService {
    /**
     * 将文本向量化并存储到Milvus
     * @param text 要向量化的文本
     * @param userId 用户ID
     * @return 向量ID
     */
    String vectorizeAndStore(String text, Long userId);
    
    /**
     * 根据文本查询相似向量
     * @param text 查询文本
     * @param userId 用户ID
     * @param limit 返回数量限制
     * @param threshold 相似度阈值
     * @return 相似记忆列表（包含ID和文本内容）
     */
    List<Map<String, Object>> searchSimilarMemories(String text, Long userId, Integer limit, Double threshold);
    
    /**
     * 根据ID获取记忆详情
     * @param memoryId 记忆ID
     * @return 记忆详情
     */
    Map<String, Object> getMemoryById(String memoryId);
    
    /**
     * 获取用户的所有记忆
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 记忆列表
     */
    List<Map<String, Object>> getUserMemories(Long userId, Integer limit);
    
    /**
     * 删除记忆
     * @param memoryId 记忆ID
     * @return 是否删除成功
     */
    boolean deleteMemory(String memoryId);
} 