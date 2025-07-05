package handler

import (
	"encoding/json"
	"io"
	"net/http"

	"lc-go/model"
	"lc-go/service"
)

// EmbeddingHandler 嵌入处理器
type EmbeddingHandler struct {
	embeddingService *service.EmbeddingService
}

// NewEmbeddingHandler 创建新的嵌入处理器
func NewEmbeddingHandler(embeddingService *service.EmbeddingService) *EmbeddingHandler {
	// TODO: 实现处理器初始化逻辑
	return &EmbeddingHandler{
		embeddingService: embeddingService,
	}
}

// GetEmbedding 处理嵌入请求
func (h *EmbeddingHandler) GetEmbedding(w http.ResponseWriter, r *http.Request) {
	// TODO: 实现嵌入请求处理逻辑
	var req model.EmbeddingRequest
	body, err := io.ReadAll(r.Body)
	if err != nil {
		sendErrorResponse(w, "无法读取请求体", http.StatusBadRequest)
		return
	}
	if err := json.Unmarshal(body, &req); err != nil {
		sendErrorResponse(w, "请求格式不合法", http.StatusBadRequest)
		return
	}

	if req.Text == "" {
		sendErrorResponse(w, "text 字段不能为空", http.StatusBadRequest)
		return
	}

	// 调用嵌入服务
	vec, err := h.embeddingService.GetEmbedding(req.Text)
	if err != nil {
		sendErrorResponse(w, "获取嵌入失败: "+err.Error(), http.StatusInternalServerError)
		return
	}

	resp := model.EmbeddingResponse{
		Success: true,
		Message: "获取成功",
		Data:    vec,
	}
	sendJSONResponse(w, resp, http.StatusOK)
}

// sendErrorResponse 发送错误响应
func sendErrorResponse(w http.ResponseWriter, message string, statusCode int) {
	// TODO: 实现错误响应逻辑
	resp := model.EmbeddingResponse{
		Success: false,
		Message: message,
	}
	sendJSONResponse(w, resp, statusCode)
}

// sendJSONResponse 发送JSON响应
func sendJSONResponse(w http.ResponseWriter, data interface{}, statusCode int) {
	// TODO: 实现JSON响应逻辑
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(statusCode)
	json.NewEncoder(w).Encode(data)
}
