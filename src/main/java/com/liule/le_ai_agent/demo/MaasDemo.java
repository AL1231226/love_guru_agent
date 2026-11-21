package com.liule.le_ai_agent.demo;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;

public class MaasDemo {

    public static void main(String[] args) {
        // 公共域名，无需 WorkspaceId
        String url = "https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation";
        String apiKey = TestApiKey.API_KEY;

        String body = """
                {
                    "model": "qwen3.8-max",
                    "input": {
                        "messages": [
                            {
                                "role": "system",
                                "content": [{"text": "You are a helpful assistant."}]
                            },
                            {
                                "role": "user",
                                "content": [{"text": "你是谁？"}]
                            }
                        ]
                    },
                    "parameters": {
                        "result_format": "message"
                    }
                }
                """;

        try (HttpResponse response = HttpRequest.post(url)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(body)
                .timeout(60_000)
                .execute()) {

            System.out.println("HTTP " + response.getStatus());
            System.out.println(response.body());
        }
    }
}
