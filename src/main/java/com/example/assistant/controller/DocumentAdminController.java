package com.example.assistant.controller;

import com.example.assistant.dto.DelVectorDTO;
import com.example.assistant.dto.DocumentListDTO;
import com.example.assistant.dto.SearchVectorDTO;
import com.example.assistant.dto.response.ApiResponse;
import com.example.assistant.entity.KnowledgeDocs;
import com.example.assistant.exception.CheckedException;
import com.example.assistant.service.DocumentIngestionService;
import com.example.assistant.service.KnowledgeDocsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentMetadata;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.util.List;

@Tag(name = "知识库管理", description = "游戏知识库文档的上传、查询与删除（管理员接口）")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/game/{gameId}/documents")
public class DocumentAdminController {

    private final DocumentIngestionService ingestionService;
    private final KnowledgeDocsService knowledgeDocsService;

    @Operation(summary = "上传文档", description = "上传文件并将其向量化写入知识库，文件大小不超过50MB")
    @PostMapping("/upload")
    public ApiResponse<Void> upload(
            @Parameter(description = "游戏ID") @PathVariable Long gameId,
            @Parameter(description = "待上传的文件") @RequestParam MultipartFile file) {
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

    @Operation(summary = "文档列表", description = "查询该游戏下已上传的知识库文档")
    @GetMapping("/list")
    public ApiResponse<List<KnowledgeDocs>> list(
            @Parameter(description = "游戏ID") @PathVariable @NotNull Long gameId,
            DocumentListDTO dto) {
        return ApiResponse.ok(knowledgeDocsService.selfGetList(gameId, dto));
    }

    @Operation(summary = "向量搜索", description = "在知识库中进行语义相似度搜索")
    @GetMapping("/search")
    public ApiResponse<List<Document>> search(
            @Parameter(description = "游戏ID") @PathVariable Long gameId,
            SearchVectorDTO dto) {
        return ApiResponse.ok(knowledgeDocsService.search(gameId, dto));
    }

    @Operation(summary = "删除文档", description = "根据文件名删除知识库中对应的所有向量数据")
    @DeleteMapping("/{filename}")
    public ApiResponse<Void> delete(
            @Parameter(description = "游戏ID") @PathVariable Long gameId,
            DelVectorDTO dto) {
        knowledgeDocsService.deleteByFilename(gameId, dto);
        return ApiResponse.ok(null);
    }
}