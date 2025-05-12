# Spring AI Application

This repository contains a Spring Boot application that integrates AI capabilities for various functionalities, including chat-based interactions, image analysis, audio processing, and retrieval-augmented generation (RAG).

## Features

### AI Chat API
- Retrieve AI-generated information about celebrities, sports players, and their achievements.
- Endpoints:
  - `/api/v1/chat/celeb`: Get detailed information about a celebrity.
  - `/api/v1/chat/player`: Get structured details about a sports player.
  - `/api/v1/chat/achievements/player`: Retrieve a list of achievements for a specific sports player.

### Image Analysis API
- Analyze and describe images using AI.
- Endpoints:
  - `/api/v1/image/describe-image`: Upload an image (PNG or JPEG) and get an AI-generated description.
  - `/api/v1/image/image-to-text`: Describe a predefined image stored in the resources directory.
  - `/api/v1/image/{prompt}`: Generate an image based on a text prompt.

### Audio Processing API
- Transcribe audio files to text and convert text to speech.
- Endpoints:
  - `/api/v1/audio/audio-to-text`: Transcribe predefined audio files.
  - `/api/v1/audio/upload-audio-to-transcribe`: Upload an audio file (MP3 or MP4) for transcription.
  - `/api/v1/audio/text-to-audio/{prompt}`: Convert text to speech and generate an MP3 file.

 ### Retrieval-Augmented Generation (RAG) API
- Answer questions using retrieval-augmented generation from a vector store.
- Endpoint:
  - `/api/v1/rag/question`: Answers questions using retrieval-augmented generation from the vector store.
  - `/api/v1/rag/pgvector-question`: Retrieves relevant documents from PGVector and generates an answer using RAG.

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.8+
- Docker (optional, for containerized deployment)

### Running the Application
1. Clone the repository:
   ```sh
   git clone https://github.com/DKichukov/spring-ai.git
   cd spring-ai
   ```

2. Build the project:
   ```sh
   mvn clean install
   ```

3. Run the application:
   ```sh
   mvn spring-boot:run
   ```

4. Access the APIs:
   - Swagger UI: `http://localhost:8080/swagger-ui.html`
   - API base URL: `http://localhost:8080/api/v1`

   ## License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
