package com.example.assistant.service.impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.document.DocumentParser;
import com.alibaba.cloud.ai.transformer.splitter.SentenceSplitter;
import com.example.assistant.service.DocumentChunkService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.tokenizer.TokenCountEstimator;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DocumentChunkServiceImpl implements DocumentChunkService {

    // 段落优先：按句子/段落切分，中文友好
    private final SentenceSplitter sentenceSplitter;
    private final TokenCountEstimator tokenCountEstimator;
    @Value("${game.assistant.overlap-ratio}")
    private double overlapRatio;

    // 兜底：按 token 切分 + overlap
    // 构造方法：chunkSize, minChunkSizeChars, minChunkLengthToEmbed, maxNumChunks, keepSeparator
    private final TokenTextSplitter tokenSplitter = new TokenTextSplitter(
            400,   // chunkSize：目标 token 数
            50,    // minChunkSizeChars：最小字符数
            5,     // minChunkLengthToEmbed：最小嵌入长度
            10000, // maxNumChunks：最大块数
            true   // keepSeparator：保留分隔符
    );

    @Override
    public List<Document> split(Document document) {
        // 1. 先尝试段落优先切分
        List<Document> chunks = sentenceSplitter.apply(List.of(document));

        // 2. 检查是否有超长块，超长的用 token 切分兜底
        List<Document> result = new ArrayList<>();
        for (Document chunk : chunks) {
            String text = Optional.ofNullable(chunk.getText()).orElse("");
            if (tokenCountEstimator.estimate(text) > 500) {
                // 超长 → 走滑动窗口兜底
                result.addAll(tokenSplitter.apply(List.of(chunk)));
            } else {
                result.add(chunk);
            }
        }

        // 3. 手动实现 overlap（Spring AI 原生不支持）
        // 取每个chunk最后的一部分加入每个chunk的前面，牺牲一部分空间换取在兜底策略时的检索精度
        return addOverlap(result);
    }

    // 滑动窗口 overlap 实现
    private List<Document> addOverlap(List<Document> chunks) {
        if (chunks.size() <= 1) return chunks;

        List<Document> overlapped = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            String text = chunks.get(i).getText();

            if (i > 0) {
                String prevText = chunks.get(i - 1).getText();
                // 根据当前块的实际 token 数动态计算 overlap 大小
                int currentTokens = tokenCountEstimator.estimate(text);
                int overlapTokens = (int) (currentTokens * overlapRatio);
                String tail = extractTail(prevText, overlapTokens);
                text = tail + text;
            }

            overlapped.add(Document.builder()
                    .text(text)
                    .metadata(new HashMap<>(chunks.get(i).getMetadata()))
                    .build());
        }
        return overlapped;
    }


    // 提取文本尾部约 overlapTokens 个字符
    private String extractTail(String text, int overlapTokens) {
        if (text == null || text.isBlank() || overlapTokens <= 0) {
            return "";
        }
        if (text.length() <= overlapTokens) return text;
        return text.substring(text.length() - overlapTokens);
    }
}