package org.crystallen.lc.service.imp;

import lombok.extern.slf4j.Slf4j;
import org.crystallen.lc.dto.LlmRequestDTO;
import org.crystallen.lc.dto.LlmResponseDTO;
import org.crystallen.lc.service.LlmService;
import org.crystallen.lc.service.VectorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LlmServiceImpl implements LlmService {

    @Value("${llm.api.url}")
    private String llmApiUrl;

    @Value("${llm.api.key}")
    private String llmApiKey;

    @Value("${llm.api.model:gpt-3.5-turbo}")
    private String model;

    @Value("${llm.api.max-tokens:1000}")
    private Integer maxTokens;

    @Value("${llm.prompt.max-tokens:3000}")
    private Integer maxPromptTokens;

    @Value("${llm.prompt.strategy:sliding-window}")
    private String promptStrategy;

    @Value("${llm.prompt.max-user-input-length:2000}")
    private Integer maxUserInputLength;

    private final VectorService vectorService;
    private final RestTemplate restTemplate = new RestTemplate();
    
    // 存储最后一次对话的记忆总结
    private String lastMemorySummary;

    @Autowired
    public LlmServiceImpl(VectorService vectorService) {
        this.vectorService = vectorService;
    }

    @Override
    public LlmResponseDTO processLlmInput(LlmRequestDTO request, Long userId) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 处理用户输入过长的情况
            String processedUserInput = processLongUserInput(request.getUserInput());
            
            // 2. 查询相关记忆
            List<Map<String, Object>> similarMemories = vectorService.searchSimilarMemories(
                processedUserInput, 
                userId, 
                request.getMaxMemories(), 
                request.getSimilarityThreshold()
            );
            
            // 3. 处理长prompt
            String processedMemories = processLongPrompt(similarMemories, processedUserInput);
            
            // 4. 构造prompt
            String prompt = constructPrompt(processedUserInput, processedMemories, request.getContext());
            
            // 5. 调用ChatGPT API
            String aiResponse = callLlmApi(prompt);
            
            // 6. 保存新的记忆（使用记忆总结）
            String newMemoryId = saveNewMemory(processedUserInput, aiResponse, userId);
            
            // 7. 构造响应
            LlmResponseDTO response = new LlmResponseDTO();
            response.setAiResponse(aiResponse);
            response.setRelatedMemories(convertToMemoryList(similarMemories));
            response.setPrompt(prompt);
            response.setProcessingTime(System.currentTimeMillis() - startTime);
            response.setNewMemoryId(newMemoryId);
            
            return response;
            
        } catch (Exception e) {
            log.error("Error processing LLM input: ", e);
            throw new RuntimeException("LLM processing error", e);
        }
    }

    /**
     * 处理用户输入过长的情况
     */
    private String processLongUserInput(String userInput) {
        if (userInput == null || userInput.isEmpty()) {
            return userInput;
        }
        
        // 设置用户输入的最大长度（字符数）
        int maxUserInputLength = this.maxUserInputLength;
        
        if (userInput.length() <= maxUserInputLength) {
            return userInput;
        }
        
        log.warn("User input too long ({} chars), compressing...", userInput.length());
        
        // 策略1: 智能截断 - 保留开头和结尾，中间用摘要
        return compressUserInput(userInput, maxUserInputLength);
    }

    /**
     * 压缩用户输入
     */
    private String compressUserInput(String userInput, int maxLength) {
        // 如果输入包含多个段落，尝试保留最重要的部分
        String[] paragraphs = userInput.split("\n\n");
        
        if (paragraphs.length > 1) {
            // 多段落情况：保留第一段和最后一段，中间段落进行摘要
            StringBuilder compressed = new StringBuilder();
            compressed.append(paragraphs[0]);
            
            if (paragraphs.length > 2) {
                compressed.append("\n\n[中间内容摘要：");
                // 对中间段落进行简单摘要
                for (int i = 1; i < paragraphs.length - 1; i++) {
                    String paragraph = paragraphs[i];
                    if (paragraph.length() > 100) {
                        compressed.append(paragraph.substring(0, 100)).append("...");
                    } else {
                        compressed.append(paragraph);
                    }
                    if (i < paragraphs.length - 2) {
                        compressed.append(" | ");
                    }
                }
                compressed.append("]\n\n");
            } else if (paragraphs.length == 2) {
                compressed.append("\n\n");
            }
            
            compressed.append(paragraphs[paragraphs.length - 1]);
            
            String result = compressed.toString();
            if (result.length() <= maxLength) {
                return result;
            }
        }
        
        // 单段落或压缩后仍然过长：使用简单的截断策略
        int halfLength = maxLength / 2;
        String start = userInput.substring(0, halfLength);
        String end = userInput.substring(userInput.length() - halfLength);
        
        return start + "\n\n[内容过长，已省略中间部分]\n\n" + end;
    }

    /**
     * 处理长prompt的策略
     */
    private String processLongPrompt(List<Map<String, Object>> memories, String userInput) {
        if (memories == null || memories.isEmpty()) {
            return "";
        }

        switch (promptStrategy) {
            case "sliding-window":
                return slidingWindowStrategy(memories);
            case "importance-ranking":
                return importanceRankingStrategy(memories);
            case "summary-compression":
                return summaryCompressionStrategy(memories);
            case "recent-first":
                return recentFirstStrategy(memories);
            default:
                return slidingWindowStrategy(memories);
        }
    }

    /**
     * 策略1: 滑动窗口 - 保留最近的N个记忆
     */
    private String slidingWindowStrategy(List<Map<String, Object>> memories) {
        int maxMemories = 5; // 最多保留5个记忆
        List<Map<String, Object>> recentMemories = memories.stream()
                .limit(maxMemories)
                .collect(Collectors.toList());
        
        return constructMemoriesText(recentMemories);
    }

    /**
     * 策略2: 重要性排序 - 基于相似度和内容长度排序
     */
    private String importanceRankingStrategy(List<Map<String, Object>> memories) {
        // 计算每个记忆的重要性分数
        List<Map<String, Object>> scoredMemories = memories.stream()
                .map(memory -> {
                    String text = (String) memory.get("text");
                    double score = calculateImportanceScore(text);
                    Map<String, Object> scoredMemory = new HashMap<>(memory);
                    scoredMemory.put("importance_score", score);
                    return scoredMemory;
                })
                .sorted((a, b) -> Double.compare(
                    (Double) b.get("importance_score"), 
                    (Double) a.get("importance_score")
                ))
                .limit(5) // 保留最重要的5个
                .collect(Collectors.toList());
        
        return constructMemoriesText(scoredMemories);
    }

    /**
     * 策略3: 摘要压缩 - 对长记忆进行摘要
     */
    private String summaryCompressionStrategy(List<Map<String, Object>> memories) {
        List<Map<String, Object>> compressedMemories = new ArrayList<>();
        
        for (Map<String, Object> memory : memories) {
            String text = (String) memory.get("text");
            String compressedText = compressText(text);
            
            Map<String, Object> compressedMemory = new HashMap<>(memory);
            compressedMemory.put("text", compressedText);
            compressedMemories.add(compressedMemory);
        }
        
        return constructMemoriesText(compressedMemories);
    }

    /**
     * 策略4: 最近优先 - 优先保留最近的记忆
     */
    private String recentFirstStrategy(List<Map<String, Object>> memories) {
        // 假设memories已经按时间排序（最新的在前）
        return constructMemoriesText(memories.stream().limit(5).collect(Collectors.toList()));
    }

    /**
     * 计算记忆的重要性分数
     */
    private double calculateImportanceScore(String text) {
        if (text == null || text.isEmpty()) {
            return 0.0;
        }
        
        double score = 0.0;
        
        // 1. 长度分数（适中的长度得分更高）
        int length = text.length();
        if (length > 50 && length < 500) {
            score += 0.3;
        } else if (length >= 500) {
            score += 0.1;
        }
        
        // 2. 关键词分数
        String[] keywords = {"重要", "关键", "记住", "注意", "总结", "结论"};
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                score += 0.2;
            }
        }
        
        // 3. 包含数字的分数（可能包含具体信息）
        if (text.matches(".*\\d+.*")) {
            score += 0.1;
        }
        
        // 4. 包含人名的分数
        if (text.matches(".*[\\u4e00-\\u9fa5]{2,4}.*")) {
            score += 0.1;
        }
        
        return Math.min(score, 1.0);
    }

    /**
     * 压缩长文本
     */
    private String compressText(String text) {
        if (text == null || text.length() <= 200) {
            return text;
        }
        
        // 简单的压缩策略：保留开头和结尾，中间用省略号
        int maxLength = 200;
        int halfLength = maxLength / 2;
        
        if (text.length() <= maxLength) {
            return text;
        }
        
        String start = text.substring(0, halfLength);
        String end = text.substring(text.length() - halfLength);
        
        return start + "...[省略中间内容]..." + end;
    }

    @Override
    public String callLlmApi(String prompt) {
        try {
            // 检查prompt长度
            if (prompt.length() > maxPromptTokens * 4) { // 粗略估算：1个token约等于4个字符
                log.warn("Prompt too long ({} chars), truncating...", prompt.length());
                prompt = truncatePrompt(prompt);
            }
            
            // 构造ChatGPT API请求
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", List.of(message));
            requestBody.put("max_tokens", maxTokens);
            requestBody.put("temperature", 0.7);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + llmApiKey);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(llmApiUrl, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    Map<String, Object> messageResponse = (Map<String, Object>) choice.get("message");
                    String content = (String) messageResponse.get("content");
                    
                    // 尝试解析JSON格式的响应
                    try {
                        return parseChatGPTResponse(content);
                    } catch (Exception e) {
                        log.warn("Failed to parse ChatGPT response as JSON, returning raw content: {}", e.getMessage());
                        return content;
                    }
                } else {
                    log.error("No choices in ChatGPT response: {}", responseBody);
                    return "抱歉，我现在无法回答您的问题。";
                }
            } else {
                log.error("ChatGPT API call failed: {}", response.getBody());
                return "抱歉，我现在无法回答您的问题。";
            }
        } catch (Exception e) {
            log.error("Error calling ChatGPT API: ", e);
            return "抱歉，服务暂时不可用。";
        }
    }

    /**
     * 截断过长的prompt
     */
    private String truncatePrompt(String prompt) {
        int maxChars = maxPromptTokens * 4;
        
        // 保留系统提示和用户问题，截断记忆部分
        String[] parts = prompt.split("相关记忆：");
        if (parts.length > 1) {
            String systemPart = parts[0];
            String memoryPart = parts[1];
            
            // 计算系统部分需要的字符数
            int systemChars = systemPart.length();
            int availableForMemory = maxChars - systemChars - 100; // 留一些缓冲
            
            if (availableForMemory > 0 && memoryPart.length() > availableForMemory) {
                memoryPart = memoryPart.substring(0, availableForMemory) + "\n...[记忆已截断]...";
            }
            
            return systemPart + "相关记忆：" + memoryPart;
        }
        
        // 如果无法分割，直接截断
        return prompt.substring(0, maxChars) + "\n...[内容已截断]...";
    }

    @Override
    public String constructPrompt(String userInput, String memories, String context) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("你是一个有记忆的AI助手。以下是用户之前的相关对话记忆：\n\n");
        
        if (memories != null && !memories.isEmpty()) {
            prompt.append("相关记忆：\n").append(memories).append("\n\n");
        }
        
        if (context != null && !context.isEmpty()) {
            prompt.append("上下文信息：\n").append(context).append("\n\n");
        }
        
        prompt.append("用户当前问题：").append(userInput).append("\n\n");
        prompt.append("请基于以上信息，特别是相关记忆，来回答用户的问题。回答要自然、连贯，并体现出你对用户历史的了解。\n\n");
        prompt.append("请严格按照以下JSON格式返回响应：\n");
        prompt.append("{\n");
        prompt.append("  \"answer\": \"你的回答内容\",\n");
        prompt.append("  \"memory_summary\": \"总结这次对话中需要记住的关键信息，用简洁的语言描述\"\n");
        prompt.append("}\n\n");
        prompt.append("注意：memory_summary应该包含用户提供的信息或重要内容，用于后续的记忆检索。");
        
        return prompt.toString();
    }

    /**
     * 解析ChatGPT返回的JSON格式响应
     * @param content ChatGPT返回的内容
     * @return 提取的答案部分
     */
    private String parseChatGPTResponse(String content) {
        try {
            // 使用简单的字符串处理来提取JSON
            int startIndex = content.indexOf("{");
            int endIndex = content.lastIndexOf("}");
            
            if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                String jsonStr = content.substring(startIndex, endIndex + 1);
                
                // 使用Jackson或其他JSON库解析
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                Map<String, Object> responseMap = mapper.readValue(jsonStr, Map.class);
                
                String answer = (String) responseMap.get("answer");
                String memorySummary = (String) responseMap.get("memory_summary");
                
                // 将记忆总结存储到类变量中，供后续使用
                this.lastMemorySummary = memorySummary;
                
                return answer != null ? answer : content;
            } else {
                return content;
            }
        } catch (Exception e) {
            log.error("Error parsing ChatGPT response: {}", e.getMessage());
            return content;
        }
    }

    private String constructMemoriesText(List<Map<String, Object>> memories) {
        if (memories == null || memories.isEmpty()) {
            return "";
        }
        
        return memories.stream()
                .map(memory -> {
                    String text = (String) memory.get("text");
                    String id = (String) memory.get("id");
                    return String.format("- %s (ID: %s)", text, id);
                })
                .collect(Collectors.joining("\n"));
    }

    private String saveNewMemory(String userInput, String aiResponse, Long userId) {
        try {
            // 使用记忆总结而不是完整对话
            String memoryId = null;
            String contentToStore;
            if (lastMemorySummary != null && !lastMemorySummary.trim().isEmpty()) {
                contentToStore = String.format("用户: %s\nAI: %s\n记忆总结: %s", 
                    userInput, aiResponse, lastMemorySummary);
                // 向量化并存储到Milvus（通过Go服务）
                memoryId = vectorService.vectorizeAndStore(contentToStore, userId);
            } else {
                // 如果没有记忆总结，就跳过
            }
            // 清空记忆总结，为下次对话做准备
            this.lastMemorySummary = null;
            
            return memoryId;
        } catch (Exception e) {
            log.error("Error saving new memory: ", e);
            return null;
        }
    }

    private List<org.crystallen.lc.entity.Memory> convertToMemoryList(List<Map<String, Object>> memoryMaps) {
        return memoryMaps.stream()
                .map(memoryMap -> {
                    org.crystallen.lc.entity.Memory memory = new org.crystallen.lc.entity.Memory();
                    memory.setId(Long.valueOf((String) memoryMap.get("id")));
                    memory.setContent((String) memoryMap.get("text"));
                    memory.setVectorId((String) memoryMap.get("id"));
                    memory.setCategory("conversation");
                    memory.setImportance(0.5);
                    return memory;
                })
                .collect(Collectors.toList());
    }
} 