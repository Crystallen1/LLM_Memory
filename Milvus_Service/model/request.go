package model

// EmbeddingRequest 嵌入请求
type EmbeddingRequest struct {
	Text string `json:"text" validate:"required"`
}

// EmbeddingResponse 嵌入响应
type EmbeddingResponse struct {
	Success bool      `json:"success"`
	Message string    `json:"message"`
	Data    []float32 `json:"data,omitempty"`
}

// MemoryInsertRequest 内存插入请求
type MemoryInsertRequest struct {
	Text string `json:"text" validate:"required"`
	ID   string `json:"id,omitempty"`
}

// MemorySearchRequest 内存搜索请求
type MemorySearchRequest struct {
	UserID    string `json:"user_id" binding:"required"`
	QueryText string `json:"query_text" binding:"required"`
	TopK      int    `json:"top_k"` // 可选，默认 10
}

// MemorySearchResponse 内存搜索响应
type MemorySearchResponse struct {
	Success bool           `json:"success"`
	Message string         `json:"message"`
	Data    []MemoryResult `json:"data,omitempty"`
}

// MemoryResult 内存搜索结果
type MemoryResult struct {
	ID       string  `json:"id"`
	Text     string  `json:"text"`
	Distance float32 `json:"distance"`
	Score    float32 `json:"score"`
}
