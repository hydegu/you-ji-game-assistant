package com.example.assistant.service;

import org.springframework.ai.document.Document;

import java.util.List;

public interface DocumentChunkService {
    public List<Document> split(Document document);
}
