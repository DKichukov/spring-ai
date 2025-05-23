package com.example.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.File;
import java.util.List;

@Configuration
public class VectorLoader {

    private static final Logger logger = LoggerFactory.getLogger(VectorLoader.class);

    @Value("classpath:rag_data/Constitution_of_the_Republic_of_Bulgaria.pdf")
    private Resource pdfResource;

    @Bean
    SimpleVectorStore simpleVectorStore(EmbeddingModel embeddingModel) {

        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel)
                .build();

        File vectorStoreFile =
                new File("C:\\JAVA\\projects\\spring-ai\\src\\main\\resources\\vector_store.json");

        if (vectorStoreFile.exists()) {
            logger.info("Loaded Vector Store File!");
            vectorStore.load(vectorStoreFile);
        } else {

            logger.info("Creating Vector Store!");

            PdfDocumentReaderConfig config
                    = PdfDocumentReaderConfig
                    .builder()
                    .withPagesPerDocument(1)
                    .build();

            PagePdfDocumentReader reader
                    = new PagePdfDocumentReader(pdfResource, config);

            var textSplitter = new TokenTextSplitter();

            List<Document> docs =
                    textSplitter.apply(reader.get());

            vectorStore.add(docs);
            vectorStore.save(vectorStoreFile);

            logger.info("Vector Store Created Successfully");
        }
        return vectorStore;
    }

}
