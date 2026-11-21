package com.liule.imagesearchmcpserver.tools;

import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class WeatherToolTest {
    @Resource
    private WeatherTool weatherTool;

    @Test
    void getWeather() {
        String result = weatherTool.getWeather("北京");
        System.out.println(result);
        assertFalse(StrUtil.isBlank(result));
    }
}