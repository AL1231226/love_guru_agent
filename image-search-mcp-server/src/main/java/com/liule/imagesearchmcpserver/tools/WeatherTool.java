package com.liule.imagesearchmcpserver.tools;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class WeatherTool {

    private static final String WEATHER_API_URL = "https://uapis.cn/api/v1/misc/weather";

    @Value("${uapi.api-key:}")
    private String apiKey;

    @Tool(description = "Get the current weather for a city in mainland China, including weather condition, temperature, wind direction, wind power, and humidity")
    public String getWeather(
            @ToolParam(description = "City name in Chinese, e.g. 北京, or in English, e.g. Tokyo") String city) {

        Map<String, Object> params = new HashMap<>();
        params.put("city", city);
        params.put("lang", "zh");

        try {
            HttpRequest request = HttpRequest.get(WEATHER_API_URL)
                    .form(params)          // Hutool 会自动对中文做 UTF-8 URL 编码
                    .timeout(30_000);
            if (StrUtil.isNotBlank(apiKey)) {
                request.header("Authorization", "Bearer " + apiKey);
            }
            try (HttpResponse response = request.execute()) {
                return response.body();
            }
        } catch (Exception e) {
            log.warn("天气查询失败，city={}", city, e);
            return "天气查询失败：" + e.getMessage();
        }
    }
}
