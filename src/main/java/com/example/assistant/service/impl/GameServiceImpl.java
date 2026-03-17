package com.example.assistant.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.assistant.entity.Game;
import com.example.assistant.service.GameService;
import com.example.assistant.mapper.GameMapper;
import org.springframework.stereotype.Service;

/**
* @author 22417
* @description 针对表【game】的数据库操作Service实现
* @createDate 2026-03-17 14:43:25
*/
@Service
public class GameServiceImpl extends ServiceImpl<GameMapper, Game>
    implements GameService{

}




