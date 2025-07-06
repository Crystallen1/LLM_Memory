package org.crystallen.lc.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

@Data
public class LlmRequestDTO implements Serializable {
    @NotBlank(message = "用户输入不能为空")
    private String userInput;
    
    private String context; // 额外的上下文信息
    private List<String> categories; // 指定查询的记忆分类
    private Integer maxMemories = 5; // 最大返回记忆数量，默认5个
    private Double similarityThreshold = 0.7; // 相似度阈值，默认0.7
} 