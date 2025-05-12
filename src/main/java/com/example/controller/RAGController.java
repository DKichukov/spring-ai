package com.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/rag")
@Tag(name = "RAG API", description = "Retrieval-Augmented Generation endpoints")
public class RAGController {

    private static final Logger logger = LoggerFactory.getLogger(RAGController.class);
    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final String prompt = """
            Answer the question using the information provided in the DOCUMENTS section.
            If the answer is not found or you're unsure, respond with "I don't know."
            
            QUESTION:
            {input}
            
            DOCUMENTS:
            {documents}
            """;

    public RAGController(ChatClient.Builder chatClient,
                         VectorStore vectorStore) {

        this.chatClient = chatClient.build();
        this.vectorStore = vectorStore;
    }

    @Operation(
            summary = "Answer question using RAG with local vector store",
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

    @Operation(
            summary = "Answer question using PGVector store",
            description = "Retrieves relevant documents from PGVector and generates an answer using RAG",
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
                            description = "Invalid or empty question"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Error processing the question"
                    )
            }
    )
    @GetMapping("/pgvector-question")
    public String answerQuestionFromPGVectorStore(
            @Parameter(
                    description = "Question to answer",
                    required = true,
                    example = "What is the capital of Bulgaria?"
            )
            @NotBlank @RequestParam String q) {

        PromptTemplate template
                = new PromptTemplate(prompt);

        Map<String, Object> promptParams
                = new HashMap<>();

        promptParams.put("input", q);
        promptParams.put("documents", findSimilarData(q));

        return chatClient
                .prompt(template.create(promptParams))
                .call()
                .content();
    }

    private String findSimilarData(String q) {

        List<Document> documents =
                vectorStore.similaritySearch(
                        SearchRequest
                                .builder()
                                .query(q)
                                .topK(5)
                                .build());

        if (documents == null || documents.isEmpty()) {
            logger.debug("No similar documents found for query: {}", q);
            return "";
        }
        return documents
                .stream()
                .map(Document::getFormattedContent)
                .collect(Collectors.joining());
    }

}

