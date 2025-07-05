package milvus

import (
	"context"
	"fmt"
	"lc-go/config"
	"lc-go/model"
	"log"
	"strconv"

	"github.com/milvus-io/milvus-sdk-go/v2/client"
	"github.com/milvus-io/milvus-sdk-go/v2/entity"
)

// Client Milvus客户端
type Client struct {
	config     *config.MilvusConfig
	collection string
	client     client.Client
}

// NewClient 创建新的Milvus客户端
func NewClient(cfg *config.MilvusConfig) (*Client, error) {
	fmt.Println("[调试] NewClient 传入 CollectionName:", cfg.CollectionName) // 调试用
	clientCfg := client.Config{
		Address: fmt.Sprintf("%s:%d", cfg.Host, cfg.Port),
	}

	mc, err := client.NewClient(context.Background(), clientCfg)
	if err != nil {
		return nil, fmt.Errorf("failed to connect to Milvus: %w", err)
	}

	cli := &Client{
		config:     cfg,
		collection: cfg.CollectionName,
		client:     mc,
	}

	fmt.Println("[调试] NewClient cli.collection:", cli.collection) // 调试用

	// 初始化集合
	if err := cli.InitCollection(); err != nil {
		return nil, fmt.Errorf("failed to initialize collection: %w", err)
	}

	return cli, nil
}

// InitCollection 初始化集合
func (cli *Client) InitCollection() error {
	// 检查集合是否存在
	exists, err := cli.client.HasCollection(context.Background(), cli.collection)
	if err != nil {
		return fmt.Errorf("failed to check collection existence: %w", err)
	}

	if !exists {
		// 创建集合
		if err := cli.createCollection(); err != nil {
			return fmt.Errorf("failed to create collection: %w", err)
		}

		// 创建索引
		if err := cli.createIndex(); err != nil {
			return fmt.Errorf("failed to create index: %w", err)
		}

		// 加载集合
		if err := cli.loadCollection(); err != nil {
			return fmt.Errorf("failed to load collection: %w", err)
		}

		log.Printf("Collection '%s' created and loaded successfully", cli.collection)
	} else {
		// 检查索引是否存在
		indexExists, err := cli.hasIndex()
		if err != nil {
			return fmt.Errorf("failed to check index existence: %w", err)
		}

		if !indexExists {
			// 创建索引
			if err := cli.createIndex(); err != nil {
				return fmt.Errorf("failed to create index: %w", err)
			}
		}

		// 加载集合
		if err := cli.loadCollection(); err != nil {
			return fmt.Errorf("failed to load collection: %w", err)
		}

		log.Printf("Collection '%s' already exists and loaded", cli.collection)
	}

	return nil
}

// createCollection 创建集合
func (cli *Client) createCollection() error {
	fmt.Println("[调试] createCollection cli.collection:", cli.collection) // 调试用
	schema := entity.NewSchema().
		WithDescription("user memory collection").
		WithField(entity.NewField().WithName("id").WithDataType(entity.FieldTypeVarChar).WithIsPrimaryKey(true).WithIsAutoID(false).WithMaxLength(64)).
		WithField(entity.NewField().WithName("text").WithDataType(entity.FieldTypeVarChar).WithMaxLength(512)).
		WithField(entity.NewField().WithName("embedding").WithDataType(entity.FieldTypeFloatVector).WithDim(int64(cli.config.Dimension))).
		WithField(entity.NewField().WithName("created_at").WithDataType(entity.FieldTypeInt64)).
		WithField(entity.NewField().WithName("updated_at").WithDataType(entity.FieldTypeInt64))

	schema.CollectionName = cli.collection

	err := cli.client.CreateCollection(context.Background(), schema, 0)
	if err != nil {
		return fmt.Errorf("failed to create collection: %w", err)
	}

	log.Printf("Collection '%s' created successfully", cli.collection)
	return nil
}

// createIndex 创建索引
func (cli *Client) createIndex() error {
	// 使用FLAT索引类型
	idx, err := entity.NewIndexFlat(entity.L2)
	if err != nil {
		return fmt.Errorf("failed to create index: %w", err)
	}

	err = cli.client.CreateIndex(context.Background(), cli.collection, "embedding", idx, false)
	if err != nil {
		return fmt.Errorf("failed to create index: %w", err)
	}

	log.Printf("Index created successfully for collection '%s'", cli.collection)
	return nil
}

// hasIndex 检查索引是否存在
func (cli *Client) hasIndex() (bool, error) {
	indexes, err := cli.client.DescribeIndex(context.Background(), cli.collection, "embedding")
	if err != nil {
		return false, nil // 索引不存在
	}
	return len(indexes) > 0, nil
}

// loadCollection 加载集合
func (cli *Client) loadCollection() error {
	err := cli.client.LoadCollection(context.Background(), cli.collection, false)
	if err != nil {
		return fmt.Errorf("failed to load collection: %w", err)
	}
	return nil
}

// Insert 插入数据
func (cli *Client) Insert(memory *model.Memory) error {
	// 转换为实体
	memoryEntity := memory.ToEntity()

	// 创建列数据
	idCol := entity.NewColumnVarChar("id", []string{memoryEntity.ID})
	textCol := entity.NewColumnVarChar("text", []string{memoryEntity.Text})
	embeddingCol := entity.NewColumnFloatVector("embedding", cli.config.Dimension, [][]float32{memoryEntity.Embedding})
	createdAtCol := entity.NewColumnInt64("created_at", []int64{memoryEntity.CreatedAt.Unix()})
	updatedAtCol := entity.NewColumnInt64("updated_at", []int64{memoryEntity.UpdatedAt.Unix()})

	// 插入数据
	_, err := cli.client.Insert(context.Background(), cli.collection, "", idCol, textCol, embeddingCol, createdAtCol, updatedAtCol)
	if err != nil {
		return fmt.Errorf("failed to insert data: %w", err)
	}

	log.Printf("Successfully inserted memory with ID: %s", memory.ID)
	return nil
}

// Search 搜索相似向量
func (cli *Client) Search(embedding []float32, topK int) ([]model.MemoryResult, error) {
	if topK <= 0 {
		topK = 10 // 默认返回10个结果
	}

	// 创建搜索参数 - 使用FLAT搜索参数
	searchParam, err := entity.NewIndexFlatSearchParam()
	if err != nil {
		return nil, fmt.Errorf("failed to create search param: %w", err)
	}

	// 执行搜索
	results, err := cli.client.Search(
		context.Background(),
		cli.collection,
		[]string{},
		"",
		[]string{"id", "text"},
		[]entity.Vector{entity.FloatVector(embedding)},
		"embedding",
		entity.L2,
		topK,
		searchParam,
	)
	if err != nil {
		return nil, fmt.Errorf("failed to search: %w", err)
	}

	// 转换结果
	var memoryResults []model.MemoryResult
	for _, result := range results {
		for _, score := range result.Scores {
			// 获取ID和文本
			idField := result.Fields.GetColumn("id")
			textField := result.Fields.GetColumn("text")

			if idField == nil || textField == nil {
				continue
			}

			idCol, ok := idField.(*entity.ColumnVarChar)
			if !ok {
				continue
			}

			textCol, ok := textField.(*entity.ColumnVarChar)
			if !ok {
				continue
			}

			// 获取第一个结果
			if len(idCol.Data()) > 0 && len(textCol.Data()) > 0 {
				memoryResult := model.MemoryResult{
					ID:       idCol.Data()[0],
					Text:     textCol.Data()[0],
					Distance: score,
					Score:    1.0 / (1.0 + score), // 将距离转换为相似度分数
				}
				memoryResults = append(memoryResults, memoryResult)
			}
		}
	}

	log.Printf("Search completed, found %d results", len(memoryResults))
	return memoryResults, nil
}

// Delete 删除数据
func (cli *Client) Delete(id string) error {
	expr := fmt.Sprintf("id == '%s'", id) // 单引号
	err := cli.client.Delete(context.Background(), cli.collection, "", expr)
	if err != nil {
		return fmt.Errorf("failed to delete data: %w", err)
	}

	log.Printf("Successfully deleted memory with ID: %s", id)
	return nil
}

// GetCollectionStats 获取集合统计信息
func (cli *Client) GetCollectionStats() (int64, error) {
	stats, err := cli.client.GetCollectionStatistics(context.Background(), cli.collection)
	if err != nil {
		return 0, fmt.Errorf("failed to get collection stats: %w", err)
	}

	// 获取行数
	rowCountStr, exists := stats["row_count"]
	if !exists {
		return 0, fmt.Errorf("row_count not found in collection statistics")
	}

	rowCount, err := strconv.ParseInt(rowCountStr, 10, 64)
	if err != nil {
		return 0, fmt.Errorf("failed to parse row_count: %w", err)
	}

	log.Printf("Collection '%s' has %d rows", cli.collection, rowCount)
	return rowCount, nil
}

// Close 关闭连接
func (cli *Client) Close() error {
	return cli.client.Close()
}
