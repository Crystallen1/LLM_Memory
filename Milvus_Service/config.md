# 配置说明

本项目使用环境变量进行配置。你需要在项目根目录创建一个 `.env` 文件来设置这些环境变量。

## 创建 .env 文件

在项目根目录创建 `.env` 文件，内容如下：

```bash
# OpenAI配置
OPENAI_API_KEY=your_openai_api_key_here
OPENAI_MODEL=text-embedding-ada-002

# Milvus配置
MILVUS_HOST=localhost
MILVUS_PORT=19530
MILVUS_COLLECTION_NAME=text_embeddings
MILVUS_DIMENSION=1536

# 服务器配置
SERVER_PORT=8080
```

## 配置项说明

### OpenAI配置
- `OPENAI_API_KEY`: 你的OpenAI API密钥
- `OPENAI_MODEL`: 使用的嵌入模型，默认为 `text-embedding-ada-002`

### Milvus配置
- `MILVUS_HOST`: Milvus服务器地址，默认为 `localhost`
- `MILVUS_PORT`: Milvus服务器端口，默认为 `19530`
- `MILVUS_COLLECTION_NAME`: 向量集合名称，默认为 `text_embeddings`
- `MILVUS_DIMENSION`: 向量维度，默认为 `1536`

### 服务器配置
- `SERVER_PORT`: HTTP服务器端口，默认为 `8080`

## 注意事项

1. 确保 `.env` 文件不要提交到版本控制系统中
2. 在生产环境中，建议使用系统环境变量而不是 `.env` 文件
3. 所有配置项都有默认值，即使不设置也能正常运行（除了 `OPENAI_API_KEY`） 