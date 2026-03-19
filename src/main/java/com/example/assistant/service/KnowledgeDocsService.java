package com.example.assistant.service;

import com.example.assistant.dto.DelVectorDTO;
import com.example.assistant.dto.DocumentListDTO;
import com.example.assistant.dto.SearchVectorDTO;
import com.example.assistant.entity.KnowledgeDocs;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.ai.document.Document;

import java.util.List;

/**
* @author 22417
* @description 针对表【knowledge_docs(游戏知识库文档表)】的数据库操作Service
* @createDate 2026-03-17 15:40:02
*/
public interface KnowledgeDocsService extends IService<KnowledgeDocs> {

    /**
     * 列出文档列表（支持靠游戏id精准匹配or关键词）
     */
    public List<KnowledgeDocs> selfGetList(Long gameId,DocumentListDTO dto);

    /**
     * 按关键词搜索知识库内容（给管理员用，预览检索效果）
     */
    public List<Document> search(Long gameId,SearchVectorDTO dto);

    /**
     * 按文件名&游戏id删除知识库的知识（删除某个文档入库的所有 chunks）
     */
    public void deleteByFilename(Long gameId,DelVectorDTO dto);

    /**
     * 删除某个游戏所有的知识库
     */
    public void deleteByGameId(Long gameId);
}
