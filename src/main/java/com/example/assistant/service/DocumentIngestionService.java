package com.example.assistant.service;

import org.springframework.ai.document.Document;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentIngestionService {
    /**
     * 入库入口：读取文档 → 切分 → 向量化存储
     */
    public void ingest(Long gameId, List<Document> rawDocuments, Long fileSize, String fileName, String fileNameOrigin, String fileUrl, String fileType);

    /**
     * 从文件入库（比如上传 txt/pdf）
     */
    public void ingestFromFile(Long gameId, Resource resource, MultipartFile file, String fileName);

}
