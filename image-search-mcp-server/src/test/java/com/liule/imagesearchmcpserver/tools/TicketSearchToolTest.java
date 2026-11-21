package com.liule.imagesearchmcpserver.tools;

import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class TicketSearchToolTest {
    @Resource
    private TicketSearchTool ticketSearchTool;
    @Test
    void queryTrainTickets() {
        String result = ticketSearchTool.queryTrainTickets("北京", "上海");
        System.out.println(result);
        assertFalse(StrUtil.isBlank(result));
    }

}