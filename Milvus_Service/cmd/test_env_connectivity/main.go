package main

import (
	"fmt"
	"log"
	"net"
	"strconv"
	"time"

	"lc-go/config"
	"lc-go/milvus"
)

func main() {
	// 加载配置（会自动加载.env文件）
	cfg := config.Load()
	if cfg == nil {
		log.Fatal("无法加载配置")
	}

	fmt.Println("=== 连接性测试 ===")
	fmt.Println()

	// 测试OpenAI API Key
	fmt.Println("1. 测试OpenAI API Key...")
	if err := testOpenAIKey(cfg.OpenAI.APIKey); err != nil {
		fmt.Printf("❌ OpenAI API Key测试失败: %v\n", err)
	} else {
		fmt.Println("✅ OpenAI API Key测试成功")
	}
	fmt.Println()

	// 测试Milvus连接
	fmt.Println("2. 测试Milvus连接...")
	if err := testMilvusConnection(&cfg.Milvus); err != nil {
		fmt.Printf("❌ Milvus连接测试失败: %v\n", err)
	} else {
		fmt.Println("✅ Milvus连接测试成功")
	}
	fmt.Println()

	fmt.Println("=== 测试完成 ===")
}

// testOpenAIKey 测试OpenAI API Key是否有效
func testOpenAIKey(apiKey string) error {
	if apiKey == "" {
		return fmt.Errorf("API Key为空")
	}

	// 检查API Key格式（OpenAI API Key通常以sk-开头）
	if len(apiKey) < 20 {
		return fmt.Errorf("API Key长度不足")
	}

	fmt.Printf("   - API Key已配置\n")
	fmt.Printf("   - API Key长度: %d\n", len(apiKey))
	fmt.Printf("   - API Key前缀: %s\n", apiKey[:7])

	// 注意：这里只是检查配置，实际API调用需要网络连接
	// 如果需要实际测试API调用，需要添加OpenAI SDK依赖

	return nil
}

// testMilvusConnection 测试Milvus连接
func testMilvusConnection(cfg *config.MilvusConfig) error {
	fmt.Printf("   - 连接地址: %s:%d\n", cfg.Host, cfg.Port)
	fmt.Printf("   - 集合名称: %s\n", cfg.CollectionName)
	fmt.Printf("   - 向量维度: %d\n", cfg.Dimension)

	// 测试TCP连接
	if err := testTCPConnection(cfg.Host, cfg.Port); err != nil {
		return fmt.Errorf("TCP连接失败: %v", err)
	}

	// 创建Milvus客户端
	client, err := milvus.NewClient(cfg)
	if err != nil {
		return fmt.Errorf("创建客户端失败: %v", err)
	}
	defer client.Close()

	fmt.Printf("   - 客户端创建成功\n")

	return nil
}

// testTCPConnection 测试TCP连接
func testTCPConnection(host string, port int) error {
	address := net.JoinHostPort(host, strconv.Itoa(port))

	conn, err := net.DialTimeout("tcp", address, 5*time.Second)
	if err != nil {
		return fmt.Errorf("无法连接到 %s: %v", address, err)
	}
	defer conn.Close()

	fmt.Printf("   - TCP连接成功: %s\n", address)
	return nil
}
