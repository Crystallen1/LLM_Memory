package config

import (
	"os"
	"strconv"

	"github.com/joho/godotenv"
)

// Config 应用配置结构
type Config struct {
	OpenAI OpenAIConfig
	Milvus MilvusConfig
	Server ServerConfig
}

// OpenAIConfig OpenAI配置
type OpenAIConfig struct {
	APIKey string
	Model  string
}

// MilvusConfig Milvus配置
type MilvusConfig struct {
	Host           string
	Port           int
	CollectionName string
	Dimension      int
}

// ServerConfig 服务器配置
type ServerConfig struct {
	Port int
}

// Load 加载配置
func Load() *Config {
	// 加载.env文件到环境变量
	if err := godotenv.Load(); err != nil {
		// 如果.env文件不存在或无法加载，继续使用环境变量
		// 这里不打印错误，因为.env文件是可选的
	}

	// 从环境变量加载配置
	cfg := &Config{
		OpenAI: OpenAIConfig{
			APIKey: getEnv("OPENAI_API_KEY", ""),
			Model:  getEnv("OPENAI_MODEL", "text-embedding-ada-002"),
		},
		Milvus: MilvusConfig{
			Host:           getEnv("MILVUS_HOST", "localhost"),
			Port:           getEnvAsInt("MILVUS_PORT", 19530),
			CollectionName: getEnv("MILVUS_COLLECTION_NAME", "text_embeddings"),
			Dimension:      getEnvAsInt("MILVUS_DIMENSION", 1536),
		},
		Server: ServerConfig{
			Port: getEnvAsInt("SERVER_PORT", 8080),
		},
	}

	return cfg
}

// getEnv 获取环境变量，如果不存在则返回默认值
func getEnv(key, defaultValue string) string {
	if value := os.Getenv(key); value != "" {
		return value
	}
	return defaultValue
}

// getEnvAsInt 获取环境变量并转换为整数，如果不存在或转换失败则返回默认值
func getEnvAsInt(key string, defaultValue int) int {
	if value := os.Getenv(key); value != "" {
		if intValue, err := strconv.Atoi(value); err == nil {
			return intValue
		}
	}
	return defaultValue
}
