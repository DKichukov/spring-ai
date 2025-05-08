package com.example.controllers;

import com.example.constants.PromptConstants;
import com.example.models.Achievement;
import com.example.models.Player;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
public class AIChatController {

    private final ChatClient chatClient;

    @Value("classpath:prompts/celeb-details.st")
    private Resource celebPrompt;

    public AIChatController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping
    public String prompt(@RequestParam @NotNull String message) {
        return Objects.requireNonNull(chatClient.prompt(message)
                        .call()
                        .chatResponse())
                .getResult()
                .getOutput()
                .getText();
    }

    @GetMapping("/celeb")
    public String getCelebDetails(@RequestParam @NotNull String name) {

//        String message = """
//                List the details of the Famous personality {name}}
//                along with their Carrier achievements.
//                Show the details in the readable format
//                """;

//        PromptTemplate template = new PromptTemplate(message);
//        PromptTemplate template = new PromptTemplate(PromptConstants.CELEB_PROMPT_TEMPLATE);
        PromptTemplate template = new PromptTemplate(celebPrompt);

        Prompt prompt = template.create(
                Map.of("name", name)
        );

        return Objects.requireNonNull(chatClient.prompt(prompt)
                        .call()
                        .chatResponse())
                .getResult()
                .getOutput()
                .getText();
    }

    @GetMapping("/player")
    public Player getSportsDetails(@RequestParam @NotNull String name) {

        BeanOutputConverter<Player> converter = new BeanOutputConverter<>(Player.class);

        UserMessage userMessage = new UserMessage(String.format(PromptConstants.PLAYER_USER_PROMPT_TEMPLATE, name));
        SystemMessage systemMessage = new SystemMessage(PromptConstants.PLAYER_SYSTEM_PROMPT);

        Prompt prompt = new Prompt(List.of(userMessage, systemMessage));

        String responseText = Objects.requireNonNull(chatClient.prompt(prompt)
                        .call()
                        .chatResponse())
                .getResult()
                .getOutput()
                .getText();

        assert responseText != null;
        return converter.convert(responseText);
    }

    @GetMapping("/achievements/player")
    public List<Achievement> getPlayerAchievements(@RequestParam @NotNull String name) {

        var message = PromptConstants.PLAYER_ACHIEVEMENT;

        PromptTemplate template = new PromptTemplate(message);

        Prompt prompt = template.create(Map.of("player", name));
        return chatClient.prompt(prompt).call().entity(new ParameterizedTypeReference<List<Achievement>>() {
        });
    }
}
