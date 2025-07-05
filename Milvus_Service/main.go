package main

import (
	"context"
	"fmt"
	"log"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"lc-go/config"
	"lc-go/milvus"
	routerpkg "lc-go/router"
	"lc-go/service"

	"github.com/gorilla/mux"
)

func main() {
	// 加载配置
	cfg := config.Load()
	if cfg == nil {
		log.Fatal("无法加载配置")
	}

	// 初始化Milvus客户端
	milvusClient, err := milvus.NewClient(&cfg.Milvus)
	if err != nil {
		log.Fatalf("初始化Milvus客户端失败: %v", err)
	}
	defer func() {
		if err := milvusClient.Close(); err != nil {
			log.Printf("关闭Milvus连接失败: %v", err)
		}
	}()

	// 初始化嵌入服务
	embeddingService := service.NewEmbeddingService(&cfg.OpenAI)

	// 初始化内存服务
	memoryService := service.NewMemoryService(embeddingService, milvusClient)

	// 创建路由器
	router := mux.NewRouter()

	// 设置路由
	routerpkg.SetupRoutes(router, embeddingService, memoryService)

	// 创建HTTP服务器
	server := &http.Server{
		Addr:         fmt.Sprintf(":%d", cfg.Server.Port),
		Handler:      router,
		ReadTimeout:  15 * time.Second,
		WriteTimeout: 15 * time.Second,
		IdleTimeout:  60 * time.Second,
	}

	// 启动服务器
	go func() {
		log.Printf("服务器启动在端口 %d", cfg.Server.Port)
		if err := server.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			log.Fatalf("服务器启动失败: %v", err)
		}
	}()

	// 等待中断信号
	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	<-quit

	log.Println("正在关闭服务器...")

	// 优雅关闭服务器
	ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)
	defer cancel()

	if err := server.Shutdown(ctx); err != nil {
		log.Fatalf("服务器强制关闭: %v", err)
	}

	log.Println("服务器已关闭")
}
