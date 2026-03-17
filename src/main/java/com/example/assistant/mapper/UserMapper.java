package com.example.assistant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.assistant.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    User selectByUsername(String username);
}
