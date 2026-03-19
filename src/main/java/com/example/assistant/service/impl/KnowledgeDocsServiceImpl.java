package com.example.assistant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.assistant.component.GameVectorStoreFactory;
import com.example.assistant.dto.DelVectorDTO;
import com.example.assistant.dto.DocumentListDTO;
import com.example.assistant.dto.SearchVectorDTO;
import com.example.assistant.entity.KnowledgeDocs;
import com.example.assistant.exception.CheckedException;
import com.example.assistant.service.KnowledgeDocsService;
import com.example.assistant.mapper.KnowledgeDocsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author 22417
* @description 针对表【knowledge_docs(游戏知识库文档表)】的数据库操作Service实现
* @createDate 2026-03-17 15:40:02
*/
@Service
@Slf4j
@RequiredArgsConstructor
public class KnowledgeDocsServiceImpl extends ServiceImpl<KnowledgeDocsMapper, KnowledgeDocs>
    implements KnowledgeDocsService{

    private final GameVectorStoreFactory vectorStoreFactory;

    @Override
    public List<KnowledgeDocs> selfGetList(Long gameId,DocumentListDTO dto) {
        LambdaQueryWrapper<KnowledgeDocs> wrapper = new LambdaQueryWrapper<KnowledgeDocs>()
                .eq(KnowledgeDocs::getGameId, gameId);
        if(dto.getKeyword() != null && !dto.getKeyword().trim().isEmpty()){
            wrapper.and(w -> w
                    .like(KnowledgeDocs::getFileName, dto.getKeyword())
                    .or().like(KnowledgeDocs::getFileNameOrigin, dto.getKeyword())
                    .or().like(KnowledgeDocs::getFileType, dto.getKeyword()));
        }
        return this.list(wrapper);
    }

    @Override
    public List<Document> search(Long gameId,SearchVectorDTO dto) {

        return vectorStoreFactory.getStore(gameId).similaritySearch(
                SearchRequest.builder()
                        .query(dto.getKeyword())
                        .topK(10)
                        .similarityThreshold(0.5)
                        .filterExpression("gameId == '" + gameId + "'")
                        .build()
        );
    }

    @Override
    public void deleteByFilename(Long gameId,DelVectorDTO dto) {
        LambdaQueryWrapper<KnowledgeDocs> wrapper = new LambdaQueryWrapper<KnowledgeDocs>()
                .eq(KnowledgeDocs::getGameId, gameId)
                .eq(KnowledgeDocs::getFileName, dto.getFileName());
        // 1. 先删元数据表，确认记录存在
        this.remove(wrapper);
        try {
            vectorStoreFactory.getStore(gameId).delete(
                    "gameId == '" + gameId + "' && filename == '" + dto.getFileName() + "'"
            );
        } catch (Exception e) {
            // 向量库删失败：数据库记录已删，向量库残留孤儿数据
            // 记录日志，后续可以通过定时任务清理
            log.error("向量库删除失败，存在孤儿数据，gameId={}, filename={}",
                    gameId, dto.getFileName());
        }
    }

    @Override
    public void deleteByGameId(Long gameId) {
        LambdaQueryWrapper<KnowledgeDocs> wrapper = new LambdaQueryWrapper<KnowledgeDocs>()
                .eq(KnowledgeDocs::getGameId, gameId);
        this.remove(wrapper);
        try {
            vectorStoreFactory.getStore(gameId).delete(
                    "gameId == '" + gameId + "'"
            );
        } catch (Exception e) {
        // 向量库删失败：数据库记录已删，向量库残留孤儿数据
        // 记录日志，后续可以通过定时任务清理
        log.error("向量库删除失败，存在孤儿数据，gameId={}", gameId);
    }
    }
}




