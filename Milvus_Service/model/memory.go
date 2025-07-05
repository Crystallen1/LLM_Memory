package model

import (
	"time"
)

// Memory 内存数据结构
type Memory struct {
	ID        string    `json:"id"`
	Text      string    `json:"text"`
	Embedding []float32 `json:"embedding"`
	CreatedAt time.Time `json:"created_at"`
	UpdatedAt time.Time `json:"updated_at"`
}

// MemoryEntity Milvus实体结构
type MemoryEntity struct {
	ID        string    `json:"id" milvus:"name:id;type:VarChar;max_length:100;primary_key"`
	Text      string    `json:"text" milvus:"name:text;type:VarChar;max_length:65535"`
	Embedding []float32 `json:"embedding" milvus:"name:embedding;type:FloatVector;dim:1536"`
	CreatedAt time.Time `json:"created_at" milvus:"name:created_at;type:Int64"`
	UpdatedAt time.Time `json:"updated_at" milvus:"name:updated_at;type:Int64"`
}

// ToEntity 转换为Milvus实体
func (m *Memory) ToEntity() *MemoryEntity {
	return &MemoryEntity{
		ID:        m.ID,
		Text:      m.Text,
		Embedding: m.Embedding,
		CreatedAt: m.CreatedAt,
		UpdatedAt: m.UpdatedAt,
	}
}

// FromEntity 从Milvus实体转换
func (m *Memory) FromEntity(entity *MemoryEntity) {
	m.ID = entity.ID
	m.Text = entity.Text
	m.Embedding = entity.Embedding
	m.CreatedAt = entity.CreatedAt
	m.UpdatedAt = entity.UpdatedAt
}
