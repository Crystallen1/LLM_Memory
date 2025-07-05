# Go Web服务框架 - 嵌入向量与向量数据库

一个功能完整的Go Web服务框架，支持文本嵌入和向量搜索功能。

## 项目结构

```
lc-go/
├── main.go                 # 主程序入口
├── go.mod                  # Go模块文件
├── README.md              # 项目说明
├── config/                # 配置管理
│   └── config.go
├── router/                # 路由配置
│   └── routes.go
├── handler/               # HTTP处理器
│   ├── embedding.go       # 嵌入处理器
│   └── memory.go          # 内存处理器
├── service/               # 业务逻辑服务
│   ├── embedding.go       # 嵌入服务
│   └── memory.go          # 内存服务
├── milvus/                # Milvus客户端
│   └── client.go
└── model/                 # 数据模型
    ├── request.go         # 请求结构
    └── memory.go          # 内存数据结构
```

## 功能特性

- 🚀 基于Gorilla Mux的路由
- 🤖 文本嵌入服务
- 🗄️ 向量数据库支持
- 📝 文本向量化和相似度搜索
- 💾 内存存储和检索

## 许可证

MIT License 