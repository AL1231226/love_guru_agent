package com.liule.imagesearchmcpserver.tools;

import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
@Slf4j
@Service
public class TicketSearchTool {
    private static final String API_URL = "https://api.lolimi.cn/API/hc/api";

    @Tool(description = "Query train tickets between two Chinese cities. Returns train numbers, departure and arrival stations and times, and the seat list for each train")
    public String queryTrainTickets(
            @ToolParam(description = "Departure city name in Chinese, e.g. 北京") String departure,
            @ToolParam(description = "Arrival city name in Chinese, e.g. 上海") String arrival) {

        Map<String, Object> params = new HashMap<>();
        params.put("departure", departure);
        params.put("arrival", arrival);
        params.put("type", "json");

        try {
            return HttpUtil.get(API_URL, params, 30_000);
        } catch (Exception e) {
            log.warn("火车票查询失败，departure={}, arrival={}", departure, arrival, e);
            return "查询失败：" + e.getMessage();
        }
    }
    }

