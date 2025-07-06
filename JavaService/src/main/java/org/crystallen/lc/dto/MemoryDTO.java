package org.crystallen.lc.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class MemoryDTO implements Serializable {
    private Long id;
    
    @NotBlank(message = "记忆内容不能为空")
    private String content;
    
    private String category;
    private Double importance;
} 