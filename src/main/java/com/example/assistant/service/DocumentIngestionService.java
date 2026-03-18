package com.example.assistant.service;

import org.springframework.ai.document.Document;
import org.springframework.core.io.Resource;

import java.util.List;

public interface DocumentIngestionService {
    /**
     * 入库入口：读取文档 → 切分 → 向量化存储
     */
    public void ingest(Long gameId, List<Document> rawDocuments);

    /**
     * 从文件入库（比如上传 txt/pdf）
     */
    public void ingestFromFile(Long gameId, Resource file, String fileName);

}
