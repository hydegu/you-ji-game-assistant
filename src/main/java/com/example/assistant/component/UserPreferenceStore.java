package com.example.assistant.component;

import com.alibaba.cloud.ai.graph.store.StoreItem;
import com.alibaba.cloud.ai.graph.store.stores.DatabaseStore;
import com.alibaba.cloud.ai.graph.store.stores.MemoryStore;
import com.alibaba.cloud.ai.graph.store.stores.RedisStore;
import com.example.assistant.pojo.UserGamePreference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

@Data
@Component
@RequiredArgsConstructor
public class UserPreferenceStore {

    private final RedisStore memoryStore;  // 注入框架提供的底层存储
    private final ObjectMapper objectMapper;

    private static final List<String> NAMESPACE = List.of("preferences");

    public UserGamePreference load(String userId) {
        return memoryStore.getItem(NAMESPACE, userId)
                .map(item -> objectMapper.convertValue(item.getValue(), UserGamePreference.class))
                .orElse(new UserGamePreference());
    }

    public void mergeAndSave(String userId, UserGamePreference newer) {
        UserGamePreference existing = load(userId);
        existing.mergeFrom(newer);
        Map<String, Object> data = objectMapper.convertValue(existing, Map.class);
        memoryStore.putItem(StoreItem.of(NAMESPACE, userId, data));
    }
}
