package org.crystallen.lc.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Memory implements Serializable {
    private Long id;
    private Long userId;
    private String content;
    private String vectorId; // Milvus中的向量ID
    private String category; // 记忆分类
    private Double importance; // 重要性评分
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 