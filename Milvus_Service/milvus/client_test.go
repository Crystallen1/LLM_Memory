package milvus

import (
	"testing"
	"time"

	"lc-go/config"
	"lc-go/model"
)

func TestNewClient(t *testing.T) {
	cfg := &config.MilvusConfig{
		Host:           "198.176.62.17",
		Port:           19530,
		CollectionName: "test_collection",
		Dimension:      1536,
	}

	client, err := NewClient(cfg)
	if err != nil {
		t.Skipf("无法连接到Milvus服务器，跳过测试: %v", err)
	}
	defer client.Close()

	if client == nil {
		t.Fatal("客户端创建失败")
	}

	if client.collection != cfg.CollectionName {
		t.Errorf("集合名称不匹配，期望: %s, 实际: %s", cfg.CollectionName, client.collection)
	}
}

func TestInsertAndSearch(t *testing.T) {
	cfg := &config.MilvusConfig{
		Host:           "198.176.62.17",
		Port:           19530,
		CollectionName: "test_collection",
		Dimension:      1536,
	}

	client, err := NewClient(cfg)
	if err != nil {
		t.Skipf("无法连接到Milvus服务器，跳过测试: %v", err)
	}
	defer client.Close()

	// 创建测试数据
	now := time.Now()
	memory := &model.Memory{
		ID:        "test_id_1",
		Text:      "这是一个测试文本",
		Embedding: make([]float32, 1536), // 创建1536维的向量
		CreatedAt: now,
		UpdatedAt: now,
	}

	// 填充测试向量数据
	for i := range memory.Embedding {
		memory.Embedding[i] = float32(i) / 1536.0
	}

	// 插入数据
	err = client.Insert(memory)
	if err != nil {
		t.Fatalf("插入数据失败: %v", err)
	}

	// 搜索数据
	results, err := client.Search(memory.Embedding, 5)
	if err != nil {
		t.Fatalf("搜索数据失败: %v", err)
	}

	if len(results) == 0 {
		t.Fatal("搜索结果为空")
	}

	// 验证搜索结果
	found := false
	for _, result := range results {
		if result.ID == memory.ID {
			found = true
			if result.Text != memory.Text {
				t.Errorf("文本不匹配，期望: %s, 实际: %s", memory.Text, result.Text)
			}
			break
		}
	}

	if !found {
		t.Fatal("未找到插入的数据")
	}
}

func TestGetCollectionStats(t *testing.T) {
	cfg := &config.MilvusConfig{
		Host:           "198.176.62.17",
		Port:           19530,
		CollectionName: "test_collection",
		Dimension:      1536,
	}

	client, err := NewClient(cfg)
	if err != nil {
		t.Skipf("无法连接到Milvus服务器，跳过测试: %v", err)
	}
	defer client.Close()

	// 获取集合统计信息
	rowCount, err := client.GetCollectionStats()
	if err != nil {
		t.Fatalf("获取集合统计信息失败: %v", err)
	}

	if rowCount < 0 {
		t.Errorf("行数不能为负数: %d", rowCount)
	}
}

func TestDelete(t *testing.T) {
	cfg := &config.MilvusConfig{
		Host:           "198.176.62.17",
		Port:           19530,
		CollectionName: "test_collection",
		Dimension:      1536,
	}

	client, err := NewClient(cfg)
	if err != nil {
		t.Skipf("无法连接到Milvus服务器，跳过测试: %v", err)
	}
	defer client.Close()

	// 删除测试数据
	err = client.Delete("test_id_1")
	if err != nil {
		t.Fatalf("删除数据失败: %v", err)
	}
}
