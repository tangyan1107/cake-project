package com.cake.ai.controller;

import com.cake.ai.entity.vo.MessageVO;
import com.cake.ai.repositoryServer.ChatHistoryRepository;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/ai")
@Api(tags = "ai智能助手相关接口")
public class Controller {

    @Autowired
    private ChatHistoryRepository chatHistoryRepository;

    private final ChatClient gameChatClient;
    private final ChatMemory chatMemory;

    @PostMapping(value = "/help",produces = "text/event-stream;charset=utf-8")
    public String chat(@RequestParam String prompt, String Id) {
        //1.保存历史会话员工id
        System.out.println("employee_Id = " + Id);
        chatHistoryRepository.save("chat", Id);
        //获取当前时间
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        //增加时间锚点
        String timePrompt = String.format("""
                【时间规则,系统规则不显示在对话中】
                当前真实日期为：%s,所有有关“今天”，“昨天”，“本周”，“本月”等计算需以此为基准
                当商家说“某月某日”时需补上年份，不能编造日期，不清楚可向商家询问
                """,currentDate,currentDate);
        //把时间锚点拼到用户提问的最前面
        String finalPrompt = timePrompt+prompt;
        //2.请求模型
        return gameChatClient.prompt()
                .user(finalPrompt)
                .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY,Id))
                .call()
                .content();
    }

    @GetMapping("/history/{type}/{Id}")
    public List<MessageVO> getChatHistory(@PathVariable("type") String type, @PathVariable("Id") String Id){
        List<Message> messages = chatMemory.get(Id, Integer.MAX_VALUE);
        if(messages == null){
            return List.of();
        }
        log.info("查询历史记录,员工Id:{}",Id);
        return messages.stream().map(MessageVO::new).toList();
    }
}
