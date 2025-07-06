package org.crystallen.lc.controller;

import cn.dev33.satoken.stp.StpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.crystallen.lc.dto.MemoryDTO;
import org.crystallen.lc.entity.Memory;
import org.crystallen.lc.service.VectorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/memory")
@Tag(name = "记忆管理接口", description = "包含记忆的增删改查操作")
public class MemoryController {

    private final VectorService vectorService;

    @Autowired
    public MemoryController(VectorService vectorService) {
        this.vectorService = vectorService;
    }

    @Operation(summary = "创建记忆", description = "创建新的记忆条目")
    @ApiResponse(responseCode = "201", description = "创建成功")
    @ApiResponse(responseCode = "400", description = "请求参数错误")
    @PostMapping
    public ResponseEntity<Memory> createMemory(@Valid @RequestBody MemoryDTO memoryDTO) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            String memoryId = vectorService.vectorizeAndStore(memoryDTO.getContent(), userId);
            
            // 构造返回的Memory对象
            Memory memory = new Memory();
            memory.setId(Long.valueOf(memoryId));
            memory.setUserId(userId);
            memory.setContent(memoryDTO.getContent());
            memory.setVectorId(memoryId);
            memory.setCategory(memoryDTO.getCategory());
            memory.setImportance(memoryDTO.getImportance() != null ? memoryDTO.getImportance() : 0.5);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(memory);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @Operation(summary = "获取记忆", description = "根据ID获取特定记忆")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @ApiResponse(responseCode = "404", description = "记忆不存在")
    @GetMapping("/{id}")
    public ResponseEntity<Memory> getMemory(@PathVariable String id) {
        try {
            Map<String, Object> memoryData = vectorService.getMemoryById(id);
            
            Memory memory = new Memory();
            memory.setId(Long.valueOf((String) memoryData.get("id")));
            memory.setContent((String) memoryData.get("text"));
            memory.setVectorId((String) memoryData.get("id"));
            memory.setCategory("conversation");
            memory.setImportance(0.5);
            
            return ResponseEntity.ok(memory);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Operation(summary = "获取用户记忆", description = "获取当前用户的所有记忆")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @GetMapping
    public ResponseEntity<List<Memory>> getUserMemories(@RequestParam(defaultValue = "50") Integer limit) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            List<Map<String, Object>> memoryMaps = vectorService.getUserMemories(userId, limit);
            
            List<Memory> memories = memoryMaps.stream()
                    .map(memoryMap -> {
                        Memory memory = new Memory();
                        memory.setId(Long.valueOf((String) memoryMap.get("id")));
                        memory.setContent((String) memoryMap.get("text"));
                        memory.setVectorId((String) memoryMap.get("id"));
                        memory.setCategory("conversation");
                        memory.setImportance(0.5);
                        return memory;
                    })
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(memories);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "删除记忆", description = "删除指定的记忆")
    @ApiResponse(responseCode = "204", description = "删除成功")
    @ApiResponse(responseCode = "404", description = "记忆不存在")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMemory(@PathVariable String id) {
        try {
            boolean success = vectorService.deleteMemory(id);
            if (success) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
} 