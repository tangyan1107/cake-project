package com.cake.ai.config;

import com.cake.ai.constants.SystemConstants;
import com.cake.ai.service.impl.DishServiceImpl;
import com.cake.ai.tools.Tools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;



@Slf4j
@Configuration
public class CommonConfiguration {

    @Bean
    public ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ChatClient serviceChatClient(OpenAiChatModel model, ChatMemory chatMemory,Tools tools) {
        ChatClient client = ChatClient
                .builder(model)
                .defaultSystem(SystemConstants.Service_SYSTEM_PROMPT)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),//打印日志
                        new MessageChatMemoryAdvisor(chatMemory)//读取和保存会话记忆
                )
                .defaultTools(tools)
                .build();
        return client;
    }
}
