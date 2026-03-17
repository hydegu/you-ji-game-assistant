package com.example.assistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 游戏知识库文档表
 * @TableName knowledge_docs
 */
@TableName(value ="knowledge_docs")
@Data
public class KnowledgeDocs implements Serializable {
    /**
     * 文档ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 游戏ID
     */
    private String gameId;

    /**
     * 原始文件名
     */
    private String fileName;

    /**
     * 文件类型（pdf/txt/docx等）
     */
    private String fileType;

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

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        KnowledgeDocs other = (KnowledgeDocs) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getGameId() == null ? other.getGameId() == null : this.getGameId().equals(other.getGameId()))
            && (this.getFileName() == null ? other.getFileName() == null : this.getFileName().equals(other.getFileName()))
            && (this.getFileType() == null ? other.getFileType() == null : this.getFileType().equals(other.getFileType()))
            && (this.getChunkCount() == null ? other.getChunkCount() == null : this.getChunkCount().equals(other.getChunkCount()))
            && (this.getUploadedBy() == null ? other.getUploadedBy() == null : this.getUploadedBy().equals(other.getUploadedBy()))
            && (this.getCreatedTime() == null ? other.getCreatedTime() == null : this.getCreatedTime().equals(other.getCreatedTime()))
            && (this.getDeletedTime() == null ? other.getDeletedTime() == null : this.getDeletedTime().equals(other.getDeletedTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getGameId() == null) ? 0 : getGameId().hashCode());
        result = prime * result + ((getFileName() == null) ? 0 : getFileName().hashCode());
        result = prime * result + ((getFileType() == null) ? 0 : getFileType().hashCode());
        result = prime * result + ((getChunkCount() == null) ? 0 : getChunkCount().hashCode());
        result = prime * result + ((getUploadedBy() == null) ? 0 : getUploadedBy().hashCode());
        result = prime * result + ((getCreatedTime() == null) ? 0 : getCreatedTime().hashCode());
        result = prime * result + ((getDeletedTime() == null) ? 0 : getDeletedTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", gameId=").append(gameId);
        sb.append(", fileName=").append(fileName);
        sb.append(", fileType=").append(fileType);
        sb.append(", chunkCount=").append(chunkCount);
        sb.append(", uploadedBy=").append(uploadedBy);
        sb.append(", createdTime=").append(createdTime);
        sb.append(", deletedTime=").append(deletedTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}