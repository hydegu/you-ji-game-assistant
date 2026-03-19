package com.example.assistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

/**
 * 游戏知识库文档表
 * @TableName knowledge_docs
 */
@TableName(value ="knowledge_docs")
@Data
@Builder
public class KnowledgeDocs implements Serializable {
    /**
     * 文档ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 游戏ID
     */
    private Long gameId;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 原始文件名
     */
    private String fileNameOrigin;

    /**
     * 文件类型（pdf/txt/docx等）
     */
    private String fileType;

    /**
     * 文件大小（byte）
     */
    private Long fileSize;

    /**
     * 文件路径
     */
    private String fileUrl;

    /**
     * 文档分片数量
     */
    private Integer chunkCount;

    /**
     * 上传人ID（关联users表）
     */
    private Long uploadedBy;

    /**
     * 上传时间
     */
    private LocalDateTime createdTime;

    /**
     * 删除时间（逻辑删除）
     */
    private LocalDateTime deletedTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}