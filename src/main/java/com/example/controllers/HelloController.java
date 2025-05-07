package com.example.controllers;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.Objects;

@RestController
public class HelloController {

    private final ChatClient chatClient;

    public HelloController(ChatClient.Builder builder) {
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

        String message = """
                List the details of the Famous personality {name}}
                along with their Carrier achievements.
                Show the details in the readable format
                """;

        PromptTemplate template = new PromptTemplate(message);

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
}
