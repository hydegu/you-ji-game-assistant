package com.example.assistant.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.assistant.entity.KnowledgeDocs;
import com.example.assistant.service.KnowledgeDocsService;
import com.example.assistant.mapper.KnowledgeDocsMapper;
import org.springframework.stereotype.Service;

/**
* @author 22417
* @description 针对表【knowledge_docs(游戏知识库文档表)】的数据库操作Service实现
* @createDate 2026-03-17 15:40:02
*/
@Service
public class KnowledgeDocsServiceImpl extends ServiceImpl<KnowledgeDocsMapper, KnowledgeDocs>
    implements KnowledgeDocsService{

}




