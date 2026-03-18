package com.example.assistant.controller;

import com.example.assistant.dto.response.ApiResponse;
import com.example.assistant.service.DocumentIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/game/{gameId}/documents")
public class DocumentAdminController {

    private final DocumentIngestionService ingestionService;

    @PostMapping("/upload")
    public ApiResponse<Void> upload(@PathVariable Long gameId,
                                    @RequestParam MultipartFile file) {
        Resource resource = file.getResource();
        ingestionService.ingestFromFile(gameId, resource, file.getOriginalFilename());
        return ApiResponse.ok(null);
    }
}