package router

import (
	"lc-go/handler"
	"lc-go/service"
	"net/http"

	"github.com/gorilla/mux"
)

// SetupRoutes 设置路由
func SetupRoutes(
	router *mux.Router,
	embeddingService *service.EmbeddingService,
	memoryService *service.MemoryService,
) {
	memoryHandler := handler.NewMemoryHandler(memoryService)
	embeddingHandler := handler.NewEmbeddingHandler(embeddingService)

	router.HandleFunc("/embedding", embeddingHandler.GetEmbedding).Methods("POST")
	// Memory 接口
	router.HandleFunc("/memory/insert", memoryHandler.Insert).Methods("POST")
	router.HandleFunc("/memory/search", memoryHandler.Search).Methods("POST")

	// Health Check（可选）
	router.HandleFunc("/health", func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(200)
		w.Write([]byte("OK"))
	})
}
