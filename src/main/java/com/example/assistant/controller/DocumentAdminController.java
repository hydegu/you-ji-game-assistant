package com.example.assistant.controller;

import com.example.assistant.dto.DelVectorDTO;
import com.example.assistant.dto.DocumentListDTO;
import com.example.assistant.dto.SearchVectorDTO;
import com.example.assistant.dto.response.ApiResponse;
import com.example.assistant.entity.KnowledgeDocs;
import com.example.assistant.exception.CheckedException;
import com.example.assistant.service.DocumentIngestionService;
import com.example.assistant.service.KnowledgeDocsService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentMetadata;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/game/{gameId}/documents")
public class DocumentAdminController {

    private final DocumentIngestionService ingestionService;
    private final KnowledgeDocsService knowledgeDocsService;

    @PostMapping("/upload")
    public ApiResponse<Void> upload(@PathVariable Long gameId,
                                    @RequestParam MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            throw new CheckedException("文件名不能为空");
        }
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new CheckedException("非法文件名");
        }

        if (file.getSize() > 50L * 1024 * 1024) {
            throw new CheckedException("文件大小不能超过50MB");
        }

        if (file.isEmpty()) {
            throw new CheckedException("文件内容为空");
        }

        Resource resource = file.getResource();
        ingestionService.ingestFromFile(gameId, resource,file, file.getOriginalFilename());
        return ApiResponse.ok(null);
    }

    @GetMapping
    public ApiResponse<List<KnowledgeDocs>> list(@PathVariable @NotNull Long gameId, DocumentListDTO dto) {
        return ApiResponse.ok(knowledgeDocsService.selfGetList(gameId,dto));
    }

    @GetMapping("/search")
    public ApiResponse<List<Document>> search(@PathVariable Long gameId,
                                              SearchVectorDTO dto) {
        return ApiResponse.ok(knowledgeDocsService.search(gameId, dto));
    }

    @DeleteMapping("/{filename}")
    public ApiResponse<Void> delete(@PathVariable Long gameId,
                                    DelVectorDTO dto) {
        knowledgeDocsService.deleteByFilename(gameId, dto);
        return ApiResponse.ok(null);
    }
}