package com.example.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

@Component
public class PGVectorLoader {

    private static final Logger logger = LoggerFactory.getLogger(PGVectorLoader.class);
    private final VectorStore vectorStore;
    private final JdbcClient jdbcClient;

    @Value("classpath:rag_data/Constitution_of_the_Republic_of_Bulgaria.pdf")
    private Resource pdfResource;

    public PGVectorLoader(VectorStore vectorStore,
                          JdbcClient jdbcClient) {

        this.vectorStore = vectorStore;
        this.jdbcClient = jdbcClient;
    }

    @PostConstruct
    public void init() {

        Integer count = jdbcClient
                .sql("select COUNT(*) from vector_store")
                .query(Integer.class)
                .single();

        logger.info("Count of vectors in the database: {}", count);

        if (count == 0) {
            logger.info("Initializing PG Vector Store Load!!");
            PdfDocumentReaderConfig config
                    = PdfDocumentReaderConfig
                    .builder()
                    .withPagesPerDocument(1)
                    .build();

            PagePdfDocumentReader reader
                    = new PagePdfDocumentReader(pdfResource, config);

            var textSplitter = new TokenTextSplitter();

            vectorStore.accept(textSplitter.apply(reader.get()));

            logger.info("Application is Started and Ready to Serve");
        }

    }
}
