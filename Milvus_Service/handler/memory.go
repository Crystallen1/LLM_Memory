package handler

import (
	"encoding/json"
	"net/http"

	"lc-go/model"
	"lc-go/service"
)

// MemoryHandler 内存处理器
type MemoryHandler struct {
	memoryService *service.MemoryService
}

// NewMemoryHandler 创建新的内存处理器
func NewMemoryHandler(memoryService *service.MemoryService) *MemoryHandler {
	// TODO: 实现处理器初始化逻辑
	return &MemoryHandler{
		memoryService: memoryService,
	}
}

// Insert 处理内存插入请求
func (h *MemoryHandler) Insert(w http.ResponseWriter, r *http.Request) {
	// TODO: 实现内存插入处理逻辑
	var req model.MemoryInsertRequest
	if err := decodeJSONBody(r, &req); err != nil {
		sendMemoryErrorResponse(w, "Invalid request", http.StatusBadRequest)
		return
	}

	// 调用 service 获取 embedding
	embedding, err := h.memoryService.GetEmbedding(req.Text)
	if err != nil {
		sendMemoryErrorResponse(w, "Embedding failed: "+err.Error(), http.StatusInternalServerError)
		return
	}

	// 写入 memory
	err = h.memoryService.InsertMemory(req.ID, req.Text, embedding)
	if err != nil {
		sendMemoryErrorResponse(w, "Insert failed: "+err.Error(), http.StatusInternalServerError)
		return
	}

	writeJSON(w, http.StatusOK, map[string]any{
		"success": true,
		"message": "Memory inserted",
	})
}

// Search 处理内存搜索请求
func (h *MemoryHandler) Search(w http.ResponseWriter, r *http.Request) {
	var req model.MemorySearchRequest
	if err := decodeJSONBody(r, &req); err != nil {
		sendMemoryErrorResponse(w, "Invalid request", http.StatusBadRequest)
		return
	}

	// 获取 embedding 向量
	embedding, err := h.memoryService.GetEmbedding(req.QueryText)
	if err != nil {
		sendMemoryErrorResponse(w, "Embedding failed: "+err.Error(), http.StatusInternalServerError)
		return
	}

	// 搜索
	results, err := h.memoryService.SearchMemory(req.UserID, embedding, req.TopK)
	if err != nil {
		sendMemoryErrorResponse(w, "Search failed: "+err.Error(), http.StatusInternalServerError)
		return
	}

	// 返回搜索结果
	resp := model.MemorySearchResponse{
		Success: true,
		Message: "Search completed",
		Data:    results,
	}
	writeJSON(w, http.StatusOK, resp)
}

// BatchInsert 处理批量插入请求
func (h *MemoryHandler) BatchInsert(w http.ResponseWriter, r *http.Request) {
	// TODO: 实现批量插入处理逻辑
}

// sendMemoryErrorResponse 发送内存错误响应
func sendMemoryErrorResponse(w http.ResponseWriter, message string, statusCode int) {
	resp := map[string]any{
		"success": false,
		"message": message,
	}
	writeJSON(w, statusCode, resp)
}

func writeJSON(w http.ResponseWriter, statusCode int, payload any) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(statusCode)
	json.NewEncoder(w).Encode(payload)
}

func decodeJSONBody(r *http.Request, out any) error {
	return json.NewDecoder(r.Body).Decode(out)
}
