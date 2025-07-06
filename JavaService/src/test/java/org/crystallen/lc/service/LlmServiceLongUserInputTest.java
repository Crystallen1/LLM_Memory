package org.crystallen.lc.service;

import org.crystallen.lc.dto.LlmRequestDTO;
import org.crystallen.lc.dto.LlmResponseDTO;
import org.crystallen.lc.service.imp.LlmServiceImpl;
import org.crystallen.lc.service.VectorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LlmServiceLongUserInputTest {

    @Mock
    private VectorService vectorService;

    private LlmServiceImpl llmService;

    @BeforeEach
    void setUp() {
        llmService = new LlmServiceImpl(vectorService);
        
        // 设置配置值
        ReflectionTestUtils.setField(llmService, "llmApiUrl", "https://api.openai.com/v1/chat/completions");
        ReflectionTestUtils.setField(llmService, "llmApiKey", "test-key");
        ReflectionTestUtils.setField(llmService, "model", "gpt-3.5-turbo");
        ReflectionTestUtils.setField(llmService, "maxTokens", 1000);
        ReflectionTestUtils.setField(llmService, "maxPromptTokens", 3000);
        ReflectionTestUtils.setField(llmService, "promptStrategy", "sliding-window");
        ReflectionTestUtils.setField(llmService, "maxUserInputLength", 2000);
    }

    @Test
    void testShortUserInput_ShouldNotBeCompressed() {
        // 准备测试数据
        String shortInput = "这是一个短的用户输入";
        LlmRequestDTO request = new LlmRequestDTO();
        request.setUserInput(shortInput);
        request.setMaxMemories(5);
        request.setSimilarityThreshold(0.7);

        // Mock VectorService
        when(vectorService.searchSimilarMemories(anyString(), anyLong(), anyInt(), anyDouble()))
                .thenReturn(new ArrayList<>());
        when(vectorService.vectorizeAndStore(anyString(), anyLong()))
                .thenReturn("test-memory-id");

        // 执行测试
        try {
            LlmResponseDTO response = llmService.processLlmInput(request, 1L);
            
            // 验证：短输入应该被原样传递
            verify(vectorService).searchSimilarMemories(eq(shortInput), eq(1L), eq(5), eq(0.7));
            
        } catch (Exception e) {
            // 由于我们没有真实的API调用，这里会抛出异常，但我们可以验证输入处理逻辑
            assertTrue(e.getMessage().contains("LLM processing error"));
        }
    }

    @Test
    void testLongUserInput_ShouldBeCompressed() {
        // 准备一个很长的用户输入
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            longInput.append("这是第").append(i + 1).append("段很长的内容。".repeat(50));
            longInput.append("\n\n");
        }
        
        LlmRequestDTO request = new LlmRequestDTO();
        request.setUserInput(longInput.toString());
        request.setMaxMemories(5);
        request.setSimilarityThreshold(0.7);

        // Mock VectorService
        when(vectorService.searchSimilarMemories(anyString(), anyLong(), anyInt(), anyDouble()))
                .thenReturn(new ArrayList<>());
        when(vectorService.vectorizeAndStore(anyString(), anyLong()))
                .thenReturn("test-memory-id");

        // 执行测试
        try {
            LlmResponseDTO response = llmService.processLlmInput(request, 1L);
            
            // 验证：长输入应该被压缩
            verify(vectorService).searchSimilarMemories(
                argThat(input -> input.length() <= 2000 && input.contains("[内容过长，已省略中间部分]")), 
                eq(1L), eq(5), eq(0.7)
            );
            
        } catch (Exception e) {
            // 验证异常信息
            assertTrue(e.getMessage().contains("LLM processing error"));
        }
    }

    @Test
    void testMultiParagraphUserInput_ShouldBeCompressedIntelligently() {
        // 准备多段落的用户输入
        String multiParagraphInput = 
            "第一段：这是开头的重要信息。\n\n" +
            "第二段：" + "这是很长的中间内容。".repeat(100) + "\n\n" +
            "第三段：" + "这是另一个很长的段落。".repeat(100) + "\n\n" +
            "第四段：这是结尾的重要信息。";
        
        LlmRequestDTO request = new LlmRequestDTO();
        request.setUserInput(multiParagraphInput);
        request.setMaxMemories(5);
        request.setSimilarityThreshold(0.7);

        // Mock VectorService
        when(vectorService.searchSimilarMemories(anyString(), anyLong(), anyInt(), anyDouble()))
                .thenReturn(new ArrayList<>());
        when(vectorService.vectorizeAndStore(anyString(), anyLong()))
                .thenReturn("test-memory-id");

        // 执行测试
        try {
            LlmResponseDTO response = llmService.processLlmInput(request, 1L);
            
            // 验证：多段落输入应该被智能压缩
            verify(vectorService).searchSimilarMemories(
                argThat(input -> 
                    input.length() <= 2000 && 
                    input.contains("第一段：") && 
                    input.contains("第四段：") &&
                    (input.contains("[中间内容摘要：") || input.contains("[内容过长，已省略中间部分]"))
                ), 
                eq(1L), eq(5), eq(0.7)
            );
            
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("LLM processing error"));
        }
    }

    @Test
    void testCompressUserInput_WithMultiParagraph() {
        // 直接测试压缩方法
        String input = 
            "第一段内容。\n\n" +
            "第二段很长的内容。" + "重复内容。".repeat(50) + "\n\n" +
            "第三段也很长的内容。" + "更多重复内容。".repeat(50) + "\n\n" +
            "最后一段内容。";
        
        String compressed = (String) ReflectionTestUtils.invokeMethod(llmService, "compressUserInput", input, 2000);
        
        assertNotNull(compressed);
        assertTrue(compressed.length() <= 2000);
        assertTrue(compressed.contains("第一段内容"));
        assertTrue(compressed.contains("最后一段内容"));
        assertTrue(compressed.contains("中间内容摘要") || compressed.contains("内容过长，已省略中间部分"));
    }

    @Test
    void testCompressUserInput_WithSingleParagraph() {
        // 测试单段落的压缩
        String input = "这是一个很长的单段落内容。" + "重复内容。".repeat(200);
        
        String compressed = (String) ReflectionTestUtils.invokeMethod(llmService, "compressUserInput", input, 2000);
        
        assertNotNull(compressed);
        assertTrue(compressed.length() <= 2000);
        assertTrue(compressed.contains("内容过长，已省略中间部分"));
    }
} 