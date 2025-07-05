package service

import (
	"lc-go/milvus"
	"lc-go/model"
)

// MemoryService 内存服务
type MemoryService struct {
	EmbeddingService *EmbeddingService
	MilvusClient     *milvus.Client
}

// NewMemoryService 创建新的内存服务
func NewMemoryService(embeddingService *EmbeddingService, milvusClient *milvus.Client) *MemoryService {
	return &MemoryService{
		EmbeddingService: embeddingService,
		MilvusClient:     milvusClient,
	}
}

// Insert 插入文本到内存
func (s *MemoryService) Insert(text string, id string) (*model.Memory, error) {
	// TODO: 实现文本插入逻辑
	return &model.Memory{}, nil
}

// Search 搜索相似文本
func (s *MemoryService) Search(query string, topK int, threshold float32) ([]model.MemoryResult, error) {
	// TODO: 实现文本搜索逻辑
	return []model.MemoryResult{}, nil
}

// BatchInsert 批量插入文本
func (s *MemoryService) BatchInsert(texts []string, ids []string) ([]*model.Memory, error) {
	// TODO: 实现批量插入逻辑
	return []*model.Memory{}, nil
}
func (s *MemoryService) GetEmbedding(text string) ([]float32, error) {
	return s.EmbeddingService.GetEmbedding(text)
}
func (s *MemoryService) InsertMemory(id string, text string, embedding []float32) error {
	mem := &model.Memory{
		ID:        id,
		Text:      text,
		Embedding: embedding,
		// 可选：加 createdAt/updatedAt 时间戳
	}
	return s.MilvusClient.Insert(mem)
}

func (s *MemoryService) SearchMemory(userID string, embedding []float32, topK int) ([]model.MemoryResult, error) {
	results, err := s.MilvusClient.Search(embedding, topK)
	if err != nil {
		return nil, err
	}

	// （可选）按 userID 过滤或加权

	return results, nil
}
