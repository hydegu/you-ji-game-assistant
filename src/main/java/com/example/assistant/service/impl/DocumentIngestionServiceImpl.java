package com.example.assistant.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingOptions;
import com.alibaba.cloud.ai.dashscope.spec.DashScopeModel;
import com.alibaba.cloud.ai.document.DocumentParser;
import com.alibaba.cloud.ai.reader.poi.PoiDocumentReader;
import com.example.assistant.component.GameVectorStoreFactory;
import com.example.assistant.config.FileStorageProperties;
import com.example.assistant.entity.KnowledgeDocs;
import com.example.assistant.exception.BusinessException;
import com.example.assistant.exception.CheckedException;
import com.example.assistant.mapper.KnowledgeDocsMapper;
import com.example.assistant.service.DocumentChunkService;
import com.example.assistant.service.DocumentIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentMetadata;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentIngestionServiceImpl implements DocumentIngestionService {

    private final GameVectorStoreFactory factory;
    private final DocumentChunkService chunkService;
    private final TokenTextSplitter textSplitter;
    private final DocumentParser parser;
    private final KnowledgeDocsMapper knowledgeDocsMapper;
    private final Tika tika;
    private final FileStorageProperties fileStorageProperties;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    @Override
    public void ingest(Long gameId, List<Document> rawDocuments, Long fileSize, String fileName, String fileNameOrigin, String fileUrl, String fileType) {
        // 走切块策略：先按段落切，以固定token+overlap策略兜底
        // 这里不需要担心丢失metadata，因为Spring AI自动把metadata分配到了每个chunk
        List<Document> chunks = rawDocuments.stream()
                .flatMap(doc -> chunkService.split(doc).stream())
                .toList();

        // 维护数据库的文档元数据
        knowledgeDocsMapper.insert(KnowledgeDocs.builder()
                .gameId(gameId)
                .fileName(fileName)
                .fileNameOrigin(fileNameOrigin)
                .fileSize(fileSize)
                .fileType(fileType)
                .fileUrl(fileUrl)
                .chunkCount(chunks.size())
                .createdTime(LocalDateTime.now())
                .build());

        // VectorStore 内部会自动调用 EmbeddingModel 向量化，无需手动embedding
        factory.getStore(gameId).add(chunks);
    }

    @Override
    public void ingestFromFile(Long gameId, Resource resource, MultipartFile file, String fileNameOrigin) {

        List<Document> raw;
        // 创建 Reader
        try(InputStream inputStream = resource.getInputStream()) {
            raw = parser.parse(inputStream);
        } catch (IOException e) {
            throw new CheckedException("转化文档失败，请检查：1.文件后缀名 2.文件编码格式 3.其他可能的错误");
        }

        // 把文件大小（bytes）传进去
        long fileSize;
        try {
            fileSize = resource.contentLength(); // 单位：字节
        } catch (IOException e) {
            fileSize = -1; // 获取失败就存 -1，不影响主流程
        }

        String fileType;
        String extension = FileUtil.extName(fileNameOrigin);
        String fileName = IdUtil.simpleUUID() + "." + extension;
        // 构建存储路径（按日期分目录）
        String datePath = LocalDate.now().format(DATE_FORMATTER);
        String fileUrl = datePath + "/" + fileName;
        String absolutePath = Paths.get(fileStorageProperties.getLocal().getBasePath(), fileUrl).toAbsolutePath().normalize().toString();

        // 确保目录存在
        File destFile = new File(absolutePath);

        if (!destFile.getParentFile().exists()) {
            destFile.getParentFile().mkdirs();
        }

        // 保存文件
        try {
            file.transferTo(destFile);
        } catch (IOException e) {
            log.error("文件上传失败：{}", e.getMessage(), e);
            throw new BusinessException("文件上传失败");
        }
        try {
            fileType = simplifyMimeType(tika.detect(resource.getInputStream()));
        } catch (IOException e) {
            log.warn("文件类型检测失败，filename={}, cause={}", fileName, e.getMessage());
            fileType = "UNKNOWN"; // 检测失败不影响主流程，降级处理
        }

        // 2. 文本分割（将大文档分割成小块）
        List<Document> splitDocuments = textSplitter.transform(raw);
        splitDocuments.forEach(chunk ->
        {
            chunk.getMetadata().put("gameId", gameId.toString());
            chunk.getMetadata().put("fileName",fileName);
        });



        ingest(gameId, splitDocuments, fileSize, fileName, fileNameOrigin, fileUrl, fileType);
    }
    public static String simplifyMimeType(String mimeType) {
        return switch (mimeType) {
            case "application/pdf"  -> "PDF";
            case "text/plain"       -> "TXT";
            case "text/markdown"    -> "MD";
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> "DOCX";
            case "application/msword" -> "DOC";
            default -> mimeType; // 未知类型直接展示原始值
        };
    }
}
