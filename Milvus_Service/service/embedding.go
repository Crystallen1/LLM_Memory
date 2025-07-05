package service

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"net/http"

	"lc-go/config"
)

// EmbeddingService OpenAI嵌入服务
type EmbeddingService struct {
	apiKey string
	model  string
	client *http.Client
}

// NewEmbeddingService 创建新的嵌入服务
func NewEmbeddingService(cfg *config.OpenAIConfig) *EmbeddingService {
	return &EmbeddingService{
		apiKey: cfg.APIKey,
		model:  cfg.Model,
		client: &http.Client{},
	}
}

// GetEmbedding 获取文本嵌入向量
func (s *EmbeddingService) GetEmbedding(text string) ([]float32, error) {
	vecs, err := s.GetEmbeddings([]string{text})
	if err != nil {
		return nil, err
	}
	return vecs[0], nil
}

// GetEmbeddings 批量获取文本嵌入向量
func (s *EmbeddingService) GetEmbeddings(texts []string) ([][]float32, error) {
	url := "https://api.openai.com/v1/embeddings"
	reqBody := openAIEmbeddingRequest{
		Model: s.model,
		Input: texts,
	}
	bodyBytes, _ := json.Marshal(reqBody)

	req, err := http.NewRequest("POST", url, bytes.NewReader(bodyBytes))
	if err != nil {
		return nil, err
	}
	req.Header.Set("Authorization", "Bearer "+s.apiKey)
	req.Header.Set("Content-Type", "application/json")

	resp, err := s.client.Do(req)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	respBody, _ := io.ReadAll(resp.Body)
	if resp.StatusCode != 200 {
		return nil, fmt.Errorf("OpenAI 返回错误：%s", respBody)
	}

	var embeddingResp openAIEmbeddingResponse
	if err := json.Unmarshal(respBody, &embeddingResp); err != nil {
		return nil, err
	}

	// 转换 float64 → float32
	var results [][]float32
	for _, item := range embeddingResp.Data {
		vec := make([]float32, len(item.Embedding))
		for i, v := range item.Embedding {
			vec[i] = float32(v)
		}
		results = append(results, vec)
	}

	return results, nil
}

type openAIEmbeddingRequest struct {
	Model string   `json:"model"`
	Input []string `json:"input"`
}

type openAIEmbeddingResponse struct {
	Data []struct {
		Embedding []float64 `json:"embedding"`
	} `json:"data"`
}
