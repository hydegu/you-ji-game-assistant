package com.example.assistant.service.impl;

import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingOptions;
import com.alibaba.cloud.ai.dashscope.spec.DashScopeModel;
import com.alibaba.cloud.ai.document.DocumentParser;
import com.alibaba.cloud.ai.reader.poi.PoiDocumentReader;
import com.example.assistant.component.GameVectorStoreFactory;
import com.example.assistant.exception.BusinessException;
import com.example.assistant.exception.CheckedException;
import com.example.assistant.service.DocumentChunkService;
import com.example.assistant.service.DocumentIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentIngestionServiceImpl implements DocumentIngestionService {

    private final GameVectorStoreFactory factory;
    private final DocumentChunkService chunkService;
    private final TokenTextSplitter textSplitter;
    private final DocumentParser parser;

    @Override
    public void ingest(Long gameId, List<Document> rawDocuments) {
        // 走切块策略：先按段落切，以固定token+overlap策略兜底
        // 这里不需要担心丢失metadata，因为Spring AI自动把metadata分配到了每个chunk
        List<Document> chunks = rawDocuments.stream()
                .flatMap(doc -> chunkService.split(doc).stream())
                .toList();

        // VectorStore 内部会自动调用 EmbeddingModel 向量化，无需手动embedding
        factory.getStore(gameId).add(chunks);
    }

    @Override
    public void ingestFromFile(Long gameId, Resource file, String fileName) {

        List<Document> raw;
        // 创建 Reader
        try(InputStream inputStream = file.getInputStream()) {
            raw = parser.parse(inputStream);
        } catch (IOException e) {
            throw new CheckedException("转化文档失败，请检查：1.文件后缀名 2.文件编码格式 3.其他可能的错误");
        }

        // 2. 文本分割（将大文档分割成小块）
        List<Document> splitDocuments = textSplitter.transform(raw);
        splitDocuments.forEach(chunk ->
        {
            chunk.getMetadata().put("gameId", gameId.toString());
            chunk.getMetadata().put("fileName",fileName);
        });

        ingest(gameId, splitDocuments);
    }
}
