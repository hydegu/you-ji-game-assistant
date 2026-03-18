package com.example.assistant.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("users")
public class User implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    /** BCrypt 哈希 */
    private String password;

    /** USER 或 ADMIN */
    private String role;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    private LocalDateTime deletedTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}
