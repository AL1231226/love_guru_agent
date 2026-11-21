package com.liule.le_ai_agent;

import com.liule.le_ai_agent.app.LoveApp;
import com.liule.le_ai_agent.demo.rag.MyMultiQueryExpander;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.rag.Query;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

@SpringBootTest
class LeAiAgentApplicationTests {

    @Test
    void contextLoads() {
    }
    @Resource
    private MyMultiQueryExpander myMultiQueryExpander;
    @Test
    void testExpandQueries() {
        List<Query> queries = myMultiQueryExpander.expandQueries();
        Assertions.assertNotNull(queries);
    }
    @Test
    void doChatWithTools() {

        testMessage("周末想带女朋友去上海约会，推荐几个适合情侣的小众打卡地？");


        testMessage("最近和对象吵架了，看看编程导航网站（codefather.cn）的其他情侣是怎么解决矛盾的？");


        testMessage("直接下载一张适合做手机壁纸的星空情侣图片为文件");


        testMessage("执行 Python3 脚本来生成数据分析报告");


        testMessage("保存我的恋爱档案为文件");


        testMessage("生成一份‘七夕约会计划’PDF，包含餐厅预订、活动流程和礼物清单");
    }

    @Resource
    private LoveApp loveApp;
    private void testMessage(String message) {
        String chatId = UUID.randomUUID().toString();
        String answer = loveApp.doChatWithTools(message, chatId);
        Assertions.assertNotNull(answer);
    }



}
