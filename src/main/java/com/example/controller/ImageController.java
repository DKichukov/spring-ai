package com.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.io.IOException;

@RestController
@RequestMapping("/api/v1/image")
@Tag(name = "Image Analysis API", description = "Endpoints for analyzing and describing images using AI")
public class ImageController {

    private final ChatModel chatModel;
    private final ImageModel imageModel;

    public ImageController(ChatModel chatModel, ImageModel imageModel) {
        this.chatModel = chatModel;
        this.imageModel = imageModel;
    }

    @Operation(
            summary = "Describe a predefined image in resources",
            description = "Returns an AI-generated description of a predefined plane image stored in the resources/images directory"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully generated image description",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(
                                    value = "The image shows a commercial airplane flying in a clear blue sky..."
                            )
                    )
            )
    })
    @GetMapping("image-to-text")
    public String describeImage() {
        // Load the image from resources, used for testing
        String imageName = "plane.png";
        return ChatClient.create(chatModel)
                .prompt()
                .user(useSpec ->
                        useSpec.text("Explain what you see in the image")
                                .media(MimeTypeUtils.IMAGE_PNG, new ClassPathResource("images/" + imageName)))
                .call()
                .content();
    }

    @Operation(
            summary = "Describe an uploaded image",
            description = "Accepts an uploaded PNG or JPEG image and returns an AI-generated description of the image content"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully generated image description",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(
                                    value = "The image shows a detailed view of..."
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid file format or processing error",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(
                                    value = "Error: Only PNG and JPEG images are supported."
                            )
                    )
            )
    })
    @PostMapping(value = "/describe-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String describeUploadedImage(@RequestParam("file") MultipartFile file) {
        try {
            String contentType = file.getContentType();
            if (contentType == null || (!contentType.equals(MediaType.IMAGE_PNG_VALUE) &&
                    !contentType.equals(MediaType.IMAGE_JPEG_VALUE))) {
                return "Error: Only PNG and JPEG images are supported.";
            }

            ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            return ChatClient.create(chatModel)
                    .prompt()
                    .user(useSpec ->
                            useSpec.text("Explain what you see in the image")
                                    .media(MimeType.valueOf(contentType), fileResource))
                    .call()
                    .content();
        } catch (IOException e) {
            return "Error processing image: " + e.getMessage();
        }
    }

    @Operation(
            summary = "Generate an image based on a text prompt",
            description = "Creates an AI-generated image using the provided text prompt and returns the URL to the image"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully generated image",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(
                                    value = "https://example.com/generated-images/image-123456.png"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid prompt or generation parameters",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(
                                    value = "Error: Unable to process the provided prompt"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Image generation service error",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(
                                    value = "Error: Image generation service unavailable"
                            )
                    )
            )
    })
    @GetMapping("{prompt}")
    public String generateImage(@PathVariable @NotNull String prompt) {
        //https://platform.openai.com/docs/api-reference/images/create
        ImageResponse imageResponse = imageModel.call(
                new ImagePrompt(prompt, OpenAiImageOptions.builder()
                        .withN(1)
                        .withWidth(1024)
                        .withHeight(1024)
                        .withQuality("hd")
                        .build()));
        return imageResponse.getResult().getOutput().getUrl();

    }
}

