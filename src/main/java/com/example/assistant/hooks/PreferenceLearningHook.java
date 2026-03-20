package com.example.assistant.hooks;

import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.hook.HookPosition;
import com.alibaba.cloud.ai.graph.agent.hook.JumpTo;
import com.alibaba.cloud.ai.graph.agent.hook.ModelHook;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelInterceptor;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolInterceptor;
import com.alibaba.cloud.ai.graph.store.StoreItem;
import com.alibaba.cloud.ai.graph.store.stores.DatabaseStore;
import com.example.assistant.component.PreferenceExtractor;
import com.example.assistant.component.UserPreferenceStore;
import com.example.assistant.pojo.UserGamePreference;
import lombok.RequiredArgsConstructor;
import net.sf.jsqlparser.schema.Database;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Component
public class PreferenceLearningHook extends ModelHook {

    private final UserPreferenceStore preferenceStore;
    private final PreferenceExtractor extractor;

    @Override
    public String getName() {
        return "preference_learning";
    }

    @Override
    public HookPosition[] getHookPositions() {
        return new HookPosition[]{HookPosition.AFTER_MODEL};
    }

    @Override
    public CompletableFuture<Map<String, Object>> afterModel(OverAllState state, RunnableConfig config) {
        String userId = config.metadata("user_id").map(Object::toString).orElse(null);
        if (userId == null) {
            return CompletableFuture.completedFuture(Map.of());
        }

        // 提取用户输入
        List<Message> messages = (List<Message>) state.value("messages").orElse(new ArrayList<>());
        if (messages.isEmpty() || messages.size() < 2) {
            return CompletableFuture.completedFuture(Map.of());
        }

        // 加载现有偏好
        UserGamePreference prefs = preferenceStore.load(userId);
        if (prefs == null) {
            return CompletableFuture.completedFuture(Map.of());
        }
        UserGamePreference extracted = extractor.extract(messages);
        if(extracted != null){
            preferenceStore.mergeAndSave(userId,extracted);
        }
        return CompletableFuture.completedFuture(Map.of());
    }
}
