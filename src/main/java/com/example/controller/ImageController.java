package com.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/image")
@Tag(name = "Image Analysis API", description = "Endpoints for analyzing and describing images using AI")
public class ImageController {

    private final ChatModel chatModel;

    public ImageController(ChatModel chatModel) {
        this.chatModel = chatModel;
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

            // Call the AI model with the uploaded image
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
}

