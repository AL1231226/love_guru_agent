package com.liule.le_ai_agent.tools;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 基于 SearchAPI.io 的网页搜索工具，默认使用百度搜索引擎。
 */
@Slf4j
@Component
public class WebSearchTool {

    private static final String SEARCH_API_URL = "https://www.searchapi.io/api/v1/search";

    private static final int MAX_RESULTS = 5;

    private static final int TIMEOUT_MILLIS = 30_000;

    private final String apiKey;

    private final String engine;

    public WebSearchTool(
            @Value("${searchapi.api-key}") String apiKey,
            @Value("${searchapi.engine:baidu}") String engine) {
        this.apiKey = apiKey;
        this.engine = engine;
    }

    @Tool(description = "Search the web using a search engine and return the titles, snippets, and links of the top results")
    public String searchWeb(@ToolParam(description = "Search keywords") String query) {


        Map<String, Object> params = new HashMap<>();
        params.put("engine", engine);
        params.put("q", query);
        params.put("api_key", apiKey);

        try {
            String response = HttpUtil.get(SEARCH_API_URL, params, TIMEOUT_MILLIS);
            JSONObject jsonObject = JSONUtil.parseObj(response);
            JSONArray organicResults = jsonObject.getJSONArray("organic_results");
            StringBuilder sb = new StringBuilder();
            int count = Math.min(organicResults.size(), MAX_RESULTS);
            for (int i = 0; i < count; i++) {
                JSONObject result = organicResults.getJSONObject(i);
                sb.append(i + 1).append(". ")
                        .append(StrUtil.blankToDefault(result.getStr("title"), "无标题")).append("\n")
                        .append("摘要：").append(StrUtil.blankToDefault(result.getStr("snippet"), "无摘要")).append("\n")
                        .append("链接：").append(StrUtil.blankToDefault(result.getStr("link"), "无链接")).append("\n\n");
            }
            return sb.toString().trim();
        } catch (Exception e) {
            log.warn("SearchAPI 搜索请求失败，query={}", query, e);
            return "搜索请求失败，请稍后重试";
        }
    }
}
