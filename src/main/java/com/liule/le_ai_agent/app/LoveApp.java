package com.liule.le_ai_agent.app;

import com.liule.le_ai_agent.advisor.LoveAppRagCustomAdvisorFactory;
import com.liule.le_ai_agent.advisor.SimpleLoggerAdvisor;
import com.liule.le_ai_agent.chatmemory.FileBasedChatMemory;
import com.liule.le_ai_agent.demo.rag.QueryRewriter;
import com.liule.le_ai_agent.entity.Actor;
import com.liule.le_ai_agent.utils.System_Constants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;

import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@Component
public class LoveApp {
    public final ChatClient chatClient;

    public LoveApp(ChatModel dashscopeChatModel) {
        //ChatMemory chatMemory = MessageWindowChatMemory.builder().maxMessages(10).build();
        String fileDir = System.getProperty("user.dir") + "/chat-memory";
        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);
        this.chatClient = ChatClient.builder(dashscopeChatModel).defaultSystem(System_Constants.SYSTEM_PROMPT).defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build(),new SimpleLoggerAdvisor()).build();
    }
    @Resource
    private Advisor loveAppRagCloudAdvisor;
    public String doChat(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId)
                        ).advisors(loveAppRagCloudAdvisor)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }
    record LoveReport(String title, List<String> suggestions) {
    }

    public Flux<String> doChatByStream(String message, String chatId) {
        return chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId)
                        ).advisors(loveAppRagCloudAdvisor)
                .stream()
                .content();
    }

    public LoveReport doChatwithActor(String message, String chatId) {
        LoveReport loveReport = chatClient
                .prompt()
                .system(System_Constants.SYSTEM_PROMPT+"每次对话后都要生成恋爱结果，标题为{用户名}的恋爱报告，内容为建议列表")
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId)
                )
                .call()
                .entity(LoveReport.class);

        return loveReport;
    }
    public String doChatwithPicture(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId)
                )
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }
    @Resource
    private VectorStore loveAppVectorStore;
    @Resource
    private QueryRewriter queryRewriter;

    /*@Resource
    private VectorStore pgVectorVectorStore;*/
    public String doChatWithRag(String message, String chatId) {
        String rewrittenQuery = queryRewriter.doQueryRewrite(message);
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(rewrittenQuery)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))

                .advisors(new SimpleLoggerAdvisor())
                /*使用云知识库
                .advisors(loveAppActor)*/
                /*使用本地知识库
                .advisors(QuestionAnswerAdvisor.builder(loveAppVectorStore).build())*/
                /*//向量存储vector
                .advisors(QuestionAnswerAdvisor.builder(pgVectorVectorStore).build())*/
                //增强型
                .advisors(LoveAppRagCustomAdvisorFactory.createLoveAppRagCustomAdvisor(
                        loveAppVectorStore, "未知"
                ))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();

        return content;
    }
    @Resource
    private ToolCallback[] allTools;

    public String doChatWithTools(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId)
                       )
                .advisors(new SimpleLoggerAdvisor())
                .toolCallbacks(allTools)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }
    @Resource
    private ToolCallbackProvider toolCallbackProvider;

    public String doChatWithMcp(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId)
                        )

                .advisors(new SimpleLoggerAdvisor())
                .toolCallbacks(toolCallbackProvider)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }



}
