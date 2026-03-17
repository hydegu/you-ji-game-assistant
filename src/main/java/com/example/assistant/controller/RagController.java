package com.example.assistant.controller;

import com.example.assistant.service.GameRagService;
import lombok.AllArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rag")
@AllArgsConstructor
public class RagController {

    private final GameRagService ragService;

    // POST /rag/search?gameId=1001&query=如何打boss
    @GetMapping("/search")
    public List<Document> search(@RequestParam Long gameId,
                                 @RequestParam String query) {
        return ragService.search(gameId, query);
    }
}