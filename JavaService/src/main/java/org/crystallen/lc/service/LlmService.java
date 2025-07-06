package org.crystallen.lc.service;

import org.crystallen.lc.dto.LlmRequestDTO;
import org.crystallen.lc.dto.LlmResponseDTO;

public interface LlmService {
    /**
     * 处理LLM输入，包括记忆查询和响应生成
     * @param request LLM请求
     * @param userId 用户ID
     * @return LLM响应
     */
    LlmResponseDTO processLlmInput(LlmRequestDTO request, Long userId);
    
    /**
     * 调用外部LLM API
     * @param prompt 构造的prompt
     * @return LLM回复
     */
    String callLlmApi(String prompt);
    
    /**
     * 构造包含记忆的prompt
     * @param userInput 用户输入
     * @param memories 相关记忆
     * @param context 额外上下文
     * @return 构造的prompt
     */
    String constructPrompt(String userInput, String memories, String context);
} 