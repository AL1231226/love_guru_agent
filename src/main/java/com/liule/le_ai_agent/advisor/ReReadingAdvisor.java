package com.liule.le_ai_agent.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;


public class ReReadingAdvisor implements CallAdvisor, StreamAdvisor {

	private static final String RE_READING_USER_PROMPT = """
			{re_read_question}

			Read the question again: {re_read_question}
			""";

	@Override
	public int getOrder() {
		return 0;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
		return callAdvisorChain.nextCall(this.before(chatClientRequest));
	}

	private ChatClientRequest before(ChatClientRequest chatClientRequest) {
		// 取出用户原始问题
		String userText = chatClientRequest.prompt().getUserMessage().getText();

		// 把原问题拼接成“再读一遍”的提示词
		UserMessage userMessage = new UserMessage(
				RE_READING_USER_PROMPT.replace("{re_read_question}", userText));

		Map<String, Object> context = new HashMap<>(chatClientRequest.context());
		context.put("re_read_question", userText);

		Prompt augmentedPrompt = chatClientRequest.prompt()
				.mutate()
				.messages(userMessage)
				.build();

		return chatClientRequest.mutate()
				.prompt(augmentedPrompt)
				.context(context)
				.build();
	}

	@Override
	public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
		return streamAdvisorChain.nextStream(this.before(chatClientRequest));
	}
}
