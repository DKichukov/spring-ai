package com.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rag")
@Tag(name = "RAG API", description = "Retrieval-Augmented Generation endpoints")
public class RAGController {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public RAGController(ChatClient.Builder chatClient,
                         VectorStore vectorStore) {

        this.chatClient = chatClient.build();
        this.vectorStore = vectorStore;
    }

    @Operation(
            summary = "Answer question using RAG",
            description = "Answers questions using retrieval-augmented generation from the vector store",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful response",
                            content = @Content(
                                    mediaType = "text/plain",
                                    examples = @ExampleObject(
                                            value = "The capital of Bulgaria is Sofia."
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid question parameter"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Error processing the question"
                    )
            }
    )
    @GetMapping("/question")
    public String answerQuestion(
            @Parameter(
                    description = "Question to answer",
                    required = true,
                    example = "What is the capital of France?"
            )
           @NotBlank @RequestParam String q) {
        return chatClient
                .prompt()
                .user(q)
                .call()
                .content();
    }

}

