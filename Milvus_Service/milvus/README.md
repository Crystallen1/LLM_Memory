# Milvus 客户端

这是一个功能完整的Milvus向量数据库客户端，支持文本嵌入向量的存储和相似度搜索。

## 功能特性

- 🔗 自动连接和初始化Milvus服务器
- 📦 自动创建集合和索引
- 💾 支持向量数据的插入、搜索和删除
- 📊 提供集合统计信息
- 🛡️ 完善的错误处理和日志记录
- 🧪 包含完整的测试用例

## 使用方法

### 1. 创建客户端

```go
import (
    "lc-go/config"
    "lc-go/milvus"
)

// 加载配置
cfg := config.Load()

// 创建Milvus客户端
client, err := milvus.NewClient(&cfg.Milvus)
if err != nil {
    log.Fatalf("创建客户端失败: %v", err)
}
defer client.Close()
```

### 2. 插入数据

```go
import (
    "lc-go/model"
    "time"
)

// 创建内存数据
memory := &model.Memory{
    ID:        "unique_id_1",
    Text:      "这是要存储的文本内容",
    Embedding: []float32{0.1, 0.2, 0.3, ...}, // 1536维向量
    CreatedAt: time.Now(),
    UpdatedAt: time.Now(),
}

// 插入到Milvus
err := client.Insert(memory)
if err != nil {
    log.Printf("插入失败: %v", err)
}
```

### 3. 搜索相似向量

```go
// 搜索相似向量
results, err := client.Search(embedding, 10) // 返回前10个最相似的结果
if err != nil {
    log.Printf("搜索失败: %v", err)
}

// 处理搜索结果
for _, result := range results {
    fmt.Printf("ID: %s, 文本: %s, 距离: %f, 相似度: %f\n", 
        result.ID, result.Text, result.Distance, result.Score)
}
```

### 4. 删除数据

```go
// 根据ID删除数据
err := client.Delete("unique_id_1")
if err != nil {
    log.Printf("删除失败: %v", err)
}
```

### 5. 获取统计信息

```go
// 获取集合中的记录数
rowCount, err := client.GetCollectionStats()
if err != nil {
    log.Printf("获取统计信息失败: %v", err)
}
fmt.Printf("集合中共有 %d 条记录\n", rowCount)
```

## 配置说明

客户端使用以下配置参数：

```go
type MilvusConfig struct {
    Host           string // Milvus服务器地址
    Port           int    // Milvus服务器端口
    CollectionName string // 集合名称
    Dimension      int    // 向量维度
}
```

## 自动初始化

客户端创建时会自动执行以下操作：

1. **连接检查**: 验证与Milvus服务器的连接
2. **集合检查**: 检查指定的集合是否存在
3. **集合创建**: 如果集合不存在，自动创建集合
4. **索引创建**: 为向量字段创建FLAT索引
5. **集合加载**: 将集合加载到内存中以供查询

## 数据结构

### Memory 结构

```go
type Memory struct {
    ID        string    `json:"id"`         // 唯一标识符
    Text      string    `json:"text"`       // 文本内容
    Embedding []float32 `json:"embedding"`  // 向量嵌入
    CreatedAt time.Time `json:"created_at"` // 创建时间
    UpdatedAt time.Time `json:"updated_at"` // 更新时间
}
```

### MemoryResult 结构

```go
type MemoryResult struct {
    ID       string  `json:"id"`        // 记录ID
    Text     string  `json:"text"`      // 文本内容
    Distance float32 `json:"distance"`  // 距离分数
    Score    float32 `json:"score"`     // 相似度分数
}
```

## 错误处理

客户端提供详细的错误信息：

- 连接错误：网络连接问题或服务器不可用
- 集合错误：集合创建、删除或访问失败
- 索引错误：索引创建或查询失败
- 数据错误：数据插入、搜索或删除失败

## 测试

运行测试用例：

```bash
# 运行所有测试
go test ./milvus

# 运行特定测试
go test ./milvus -run TestNewClient

# 显示详细输出
go test ./milvus -v
```

注意：测试需要运行中的Milvus服务器。如果服务器不可用，测试会被跳过。

## 性能优化

1. **批量操作**: 对于大量数据，考虑使用批量插入
2. **索引选择**: 根据数据规模选择合适的索引类型
3. **连接池**: 在生产环境中使用连接池管理连接
4. **异步操作**: 对于非关键操作，考虑使用异步处理

## 注意事项

1. 确保Milvus服务器正在运行
2. 向量维度必须与配置中的维度一致
3. ID字段必须是唯一的
4. 在生产环境中，建议使用更复杂的索引类型（如IVF_FLAT）
5. 定期监控集合大小和性能指标 