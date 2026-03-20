package com.example.assistant.intercepter;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelCallHandler;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelInterceptor;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelRequest;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelResponse;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.example.assistant.pojo.AnswerValidation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.ValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnswerValidationInterceptor extends ModelInterceptor {
    private final ChatModel chatModel;
    private static final double MIN_CONFIDENCE = 0.7;
    @Qualifier("validAgent")
    private final ReactAgent validAgent;
    private final ObjectMapper objectMapper;

    @Override
    public ModelResponse interceptModel(ModelRequest request, ModelCallHandler handler) {
        // 先调用模型生成答案
        ModelResponse response = handler.call(request);
        ChatResponse chatResponse = response.getChatResponse();
        if (chatResponse == null) {
            return response;
        }

        // 这一步验证回答质量，需要注意的是，官方的示例给了错误的API，这里需要替换为ChatResponse的API
        AssistantMessage answer = chatResponse.getResult().getOutput();
        if (!answer.getToolCalls().isEmpty()) {
            return response;
        }

        // 跳过验证：空文本响应
        String text = answer.getText();
        if (text == null || text.isBlank()) {
            return response;
        }

        AnswerValidation isValid = validateAnswer(text, request);

        if (!isValid.pass()) {
            // 如果答案质量不足，可以添加提示要求重新生成
            SystemMessage validationPrompt = new SystemMessage(
                    "请重新检查你的答案，确保基于提供的上下文信息，并且准确完整。"
            );

            ModelRequest retryRequest = ModelRequest.builder(request)
                    .systemMessage(validationPrompt)
                    .build();

            // 可以选择重试或返回当前答案
            return handler.call(retryRequest);
        }

        return response;
    }

    private AnswerValidation validateAnswer(String answer, ModelRequest request) {
        String validationJson;
        try {
            // 单独捕获 Agent 调用异常
            validationJson = validAgent.call(answer).getText();
        } catch (Exception e) {
            log.warn("验证Agent调用失败，默认放行，cause={}", e.getMessage());
            return new AnswerValidation(true, "验证器调用失败默认放行", null);
        }

        // 解析 JSON
        try {
            JsonNode json = objectMapper.readTree(validationJson);
            boolean pass = json.get("pass").asBoolean();
            String reason = json.get("reason").asText();
            String violation = json.has("violation") && !json.get("violation").isNull()
                    ? json.get("violation").asText()
                    : null;

            return new AnswerValidation(pass, reason, violation);
        } catch (Exception e) {
            log.warn("验证结果解析失败，默认放行，raw={}", validationJson);
            return new AnswerValidation(true, "解析失败默认放行", null);
        }
    }

    @Override
    public String getName() {
        return "answer_validation";
    }
}