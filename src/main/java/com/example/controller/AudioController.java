package com.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/audio")
@Tag(name = "Audio Processing API", description = "Endpoints for audio transcription and processing")
public class AudioController {

    private final OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel;

    public AudioController(OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel) {

        this.openAiAudioTranscriptionModel = openAiAudioTranscriptionModel;
    }

    @Operation(
            summary = "Transcribe audio to text",
            description = "Converts a predefined audio file (song.mp3) to text using OpenAI's audio transcription model. " +
                    "The transcription is returned in SRT format with Bulgarian language detection."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully transcribed audio",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(
                                    value = """
                                            1
                                            00:00:00,000 --> 00:00:05,400
                                            Днес е прекрасен ден за разходка в парка.
                                            
                                            2
                                            00:00:05,600 --> 00:00:10,800
                                            Слънцето грее ярко на небето.
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Audio processing error",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(
                                    value = "Error: Unable to process the audio file"
                            )
                    )
            )
    })
    @GetMapping("/audio-to-text")
    public String audioTranscription() {
        // Load the audio from resources/audios, used for testing this approach
        String songTitle = "song-1.mp3";

        OpenAiAudioTranscriptionOptions options
                = OpenAiAudioTranscriptionOptions
                .builder()
                .language("bg")
                .responseFormat(OpenAiAudioApi.TranscriptResponseFormat.SRT)
                .temperature(0.5f)
                .build();

        AudioTranscriptionPrompt prompt
                = new AudioTranscriptionPrompt(
                new ClassPathResource("audios/" + songTitle)
                , options);

        return openAiAudioTranscriptionModel
                .call(prompt)
                .getResult()
                .getOutput();
    }

    @Operation(
            summary = "Upload and transcribe audio",
            description = "Converts an uploaded audio file to text using OpenAI's audio transcription model. " +
                    "Supports MP3 and MP4 formats. Allows language selection (English or Bulgarian) and returns the transcription in SRT format."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully transcribed audio",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = {
                                    @ExampleObject(
                                            name = "English Example",
                                            summary = "Example transcription in English",
                                            value = """
                                                    1
                                                    00:00:00,000 --> 00:00:05,400
                                                    Today is a beautiful day for a walk in the park.
                                                    
                                                    2
                                                    00:00:05,600 --> 00:00:10,800
                                                    The sun is shining brightly in the sky.
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Bulgarian Example",
                                            summary = "Example transcription in Bulgarian",
                                            value = """
                                                    1
                                                    00:00:00,000 --> 00:00:05,400
                                                    Днес е прекрасен ден за разходка в парка.
                                                    
                                                    2
                                                    00:00:05,600 --> 00:00:10,800
                                                    Слънцето грее ярко на небето.
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid file format or missing parameters",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(
                                    value = "Error: Only MP3 and MP4 audio files are supported."
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Audio processing error",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(
                                    value = "Error: Unable to process the audio file"
                            )
                    )
            )
    })
    @PostMapping(value = "/upload-audio-to-transcribe", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String uploadAndTranscribeAudio(
            @Parameter(
                    description = "Audio file to transcribe (MP3 or MP4)",
                    required = true,
                    content = @Content(mediaType = "audio/mpeg, audio/mp4")
            )
            @RequestParam("file") MultipartFile file,

            @Parameter(
                    description = "Language code (en for English, bg for Bulgarian)",
                    required = false,
                    schema = @Schema(type = "string", allowableValues = {"en", "bg"}, defaultValue = "en")
            )
            @RequestParam(value = "language", defaultValue = "en") String language) {

        try {
            boolean isValidAudioFile = isIsValidAudioFile(file);

            if (!isValidAudioFile) {
                return "Error: Only MP3 and MP4 audio files are supported.";
            }

            if (!language.equals("en") && !language.equals("bg")) {
                return "Error: Language must be either 'en' (English) or 'bg' (Bulgarian).";
            }

            ByteArrayResource audioResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {

                    return file.getOriginalFilename();
                }
            };

            OpenAiAudioTranscriptionOptions options = OpenAiAudioTranscriptionOptions
                    .builder()
                    .language(language)
                    .responseFormat(OpenAiAudioApi.TranscriptResponseFormat.SRT)
                    .temperature(0.5f)
                    .build();

            AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(audioResource, options);

            return openAiAudioTranscriptionModel
                    .call(prompt)
                    .getResult()
                    .getOutput();

        } catch (IOException e) {
            return "Error processing audio file: " + e.getMessage();
        }
    }

    private static boolean isIsValidAudioFile(MultipartFile file) {

        String originalFilename = file.getOriginalFilename();
        String contentType = file.getContentType();

        boolean isValidAudioFile = false;

        if (originalFilename != null) {
            String extension = originalFilename.toLowerCase();
            if (extension.endsWith(".mp3") || extension.endsWith(".mp4")) {
                isValidAudioFile = true;
            }
        }

        if (contentType != null) {
            if (contentType.equals("audio/mpeg") ||
                    contentType.equals("audio/mp3") ||
                    contentType.equals("audio/mp4") ||
                    contentType.equals("video/mp4")) {
                isValidAudioFile = true;
            }
        }
        return isValidAudioFile;
    }
}
