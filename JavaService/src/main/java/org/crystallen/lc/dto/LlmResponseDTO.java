package org.crystallen.lc.dto;

import lombok.Data;
import org.crystallen.lc.entity.Memory;
import java.io.Serializable;
import java.util.List;

@Data
public class LlmResponseDTO implements Serializable {
    private String aiResponse; // AI的回复
    private List<Memory> relatedMemories; // 相关的记忆
    private String prompt; // 构造的完整prompt（用于调试）
    private Long processingTime; // 处理时间（毫秒）
    private String newMemoryId; // 新创建的记忆ID
} 