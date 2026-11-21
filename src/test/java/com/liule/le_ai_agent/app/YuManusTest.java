package com.liule.le_ai_agent.app;

import com.liule.le_ai_agent.agent.LeManus;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class YuManusTest {  
  
    @Resource  
    private LeManus yuManus;
  
    @Test  
    void run() {  
        String userPrompt = "我的另一半居住在上海静安区，请帮我找到 5 公里内合适的约会地点";
        String answer = yuManus.run(userPrompt);  
        Assertions.assertNotNull(answer);  
    }  
}
