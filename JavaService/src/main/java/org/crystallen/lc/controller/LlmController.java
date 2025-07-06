package org.crystallen.lc.controller;

import cn.dev33.satoken.stp.StpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.crystallen.lc.dto.LlmRequestDTO;
import org.crystallen.lc.dto.LlmResponseDTO;
import org.crystallen.lc.service.LlmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/llm")
@Tag(name = "LLM记忆接口", description = "包含LLM对话和记忆功能的核心接口")
public class LlmController {

    private final LlmService llmService;

    @Autowired
    public LlmController(LlmService llmService) {
        this.llmService = llmService;
    }

    @Operation(summary = "LLM对话", description = "处理用户输入，查询相关记忆并生成AI回复")
    @ApiResponse(responseCode = "200", description = "处理成功")
    @ApiResponse(responseCode = "400", description = "请求参数错误")
    @ApiResponse(responseCode = "401", description = "未授权")
    @PostMapping("/chat")
    public ResponseEntity<LlmResponseDTO> chat(@Valid @RequestBody LlmRequestDTO request) {
        try {
            // 获取当前登录用户ID
            Long userId = StpUtil.getLoginIdAsLong();
            
            LlmResponseDTO response = llmService.processLlmInput(request, userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    private LlmResponseDTO createErrorResponse(String errorMessage) {
        LlmResponseDTO response = new LlmResponseDTO();
        response.setAiResponse("抱歉，处理您的请求时出现了错误：" + errorMessage);
        response.setProcessingTime(0L);
        return response;
    }
} 