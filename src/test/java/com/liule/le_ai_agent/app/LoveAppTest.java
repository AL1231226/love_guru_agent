package com.liule.le_ai_agent.app;

import com.liule.le_ai_agent.entity.Actor;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class LoveAppTest {
    @Resource
    private LoveApp loveApp;

    @Test
    void doChat() {
        loveApp.doChat("你好，我是刘乐", "111");
        loveApp.doChat("你好，还记得我是谁吗", "111");
    }
    @Test
    void doChatwithActor() {
        String message = "你好，我是刘乐，我想让另一半（编程导航）更爱我，但我不知道该怎么做";
        LoveApp.LoveReport loveReport= loveApp.doChatwithActor(message,"112");
        Assertions.assertNotNull(loveReport);
    }

    @Test
    void doChatWithRag() {
        String chatId = UUID.randomUUID().toString();
        String message = "我已经结婚了，但是婚后关系不太亲密，怎么办？";
        String answer =  loveApp.doChatWithRag(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Autowired
    VectorStore pgVectorVectorStore;
    @Test
    void doVector() {


// ...

        List<Document> documents = List.of(
                new Document("Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!", Map.of("meta1", "meta1")),
                new Document("The World is Big and Salvation Lurks Around the Corner"),
                new Document("You walk forward facing the past and you turn back toward the future.", Map.of("meta2", "meta2")));

// Add the documents to PGVector
        pgVectorVectorStore.add(documents);

// Retrieve documents similar to a query
        List<Document> results = this.pgVectorVectorStore.similaritySearch(SearchRequest.builder().query("Spring").topK(5).build());
        Assertions.assertNotNull(results);
        ;
    }

    @Test
    void doChatWithMcp() {
        String chatId = UUID.randomUUID().toString();
        /*String answer =  loveApp.doChatWithMcp("帮我查询大连民族大学的位置，带经纬度，我要和对象在附近约会", chatId);
        Assertions.assertNotNull(answer);*/
        String answer = loveApp.doChatWithMcp("帮我查询明天大连的天气，我要和对象在附近约会", chatId);
        Assertions.assertNotNull(answer);

    }
}