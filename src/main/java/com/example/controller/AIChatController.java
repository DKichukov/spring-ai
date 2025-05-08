package com.example.controller;

import com.example.constant.PromptConstant;
import com.example.model.Achievement;
import com.example.model.Player;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/chat")
@Tag(name = "AI Chat API", description = "Endpoints for retrieving AI-generated information about celebrities, sports players, and achievements")
public class AIChatController {

    private final ChatClient chatClient;

    @Value("classpath:prompts/celeb-details.st")
    private Resource celebPrompt;

    public AIChatController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Operation(
            summary = "General AI chat prompt",
            description = "Send a message to the AI and receive a response"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successful response from AI",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(
                                    value = "Here is the information you requested about your query..."
                            )
                    )
            )
    })
    @GetMapping
    public String prompt(@RequestParam @NotNull String message) {
        return Objects.requireNonNull(chatClient.prompt(message)
                        .call()
                        .chatResponse())
                .getResult()
                .getOutput()
                .getText();
    }

    @Operation(
            summary = "Get celebrity information",
            description = "Retrieves detailed information about a famous personality including their career achievements"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successful retrieval of celebrity information",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(
                                    value = """
                    Name: Albert Einstein
                    Born: March 14, 1879
                    Died: April 18, 1955
                    Occupation: Theoretical Physicist
                    
                    Career Achievements:
                    - Developed the theory of relativity
                    - Won the Nobel Prize in Physics in 1921
                    - Published more than 300 scientific papers
                    - E=mc² equation revolutionized physics
                    """
                            )
                    )
            )
    })
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

    @Operation(
            summary = "Get sports player details",
            description = "Retrieves structured information about a sports player in JSON format"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successful retrieval of player information",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Player.class),
                            examples = @ExampleObject(
                                    value = """
                    {
                      "name": "Lionel Messi",
                      "sport": "Football (Soccer)",
                      "nationality": "Argentina",
                      "birthDate": "1987-06-24",
                      "teams": ["FC Barcelona", "Paris Saint-Germain", "Inter Miami CF"],
                      "position": "Forward",
                      "careerSummary": "One of the greatest footballers of all time..."
                    }
                    """
                            )
                    )
            )
    })
    @GetMapping("/player")
    public Player getSportsDetails(@RequestParam @NotNull String name) {

        BeanOutputConverter<Player> converter = new BeanOutputConverter<>(Player.class);

        UserMessage userMessage = new UserMessage(String.format(PromptConstant.PLAYER_USER_PROMPT_TEMPLATE, name));
        SystemMessage systemMessage = new SystemMessage(PromptConstant.PLAYER_SYSTEM_PROMPT);

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

    @Operation(
            summary = "Get player achievements",
            description = "Retrieves a list of achievements for a specified sports player"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successful retrieval of player achievements",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Achievement.class)),
                            examples = @ExampleObject(
                                    value = """
                    [
                      {
                        "title": "FIFA World Cup Winner",
                        "year": 2022,
                        "description": "Led Argentina to victory in Qatar"
                      },
                      {
                        "title": "Ballon d'Or",
                        "year": 2021,
                        "description": "7th time winning the prestigious award"
                      }
                    ]
                    """
                            )
                    )
            )
    })
    @GetMapping("/achievements/player")
    public List<Achievement> getPlayerAchievements(@RequestParam @NotNull String name) {

        var message = PromptConstant.PLAYER_ACHIEVEMENT;

        PromptTemplate template = new PromptTemplate(message);

        Prompt prompt = template.create(Map.of("player", name));
        return chatClient.prompt(prompt).call().entity(new ParameterizedTypeReference<List<Achievement>>() {
        });
    }
}
