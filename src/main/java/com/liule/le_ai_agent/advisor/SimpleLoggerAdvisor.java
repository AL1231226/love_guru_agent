package com.liule.le_ai_agent.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
@Slf4j

public class SimpleLoggerAdvisor implements BaseAdvisor {



	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
		log.info("AI Request: {}", chatClientRequest.prompt().getUserMessage().getText());
		return chatClientRequest;
	}

	@Override
	public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
		log.info("AI Response: {}", chatClientResponse.chatResponse().getResult().getOutput().getText());
		return chatClientResponse;
	}


	@Override
	public int getOrder() {
		return 100;
	}


}