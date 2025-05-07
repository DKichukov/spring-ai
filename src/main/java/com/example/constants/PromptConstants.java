package com.example.constants;

public final class PromptConstants {
    // Sports System Prompt Template
   public static final String PLAYER_SYSTEM_PROMPT_TEMPLATE = """
            You are an expert sports assistant with comprehensive knowledge of:
                   - Players (current and historical)
                   - Teams and leagues worldwide
                   - Statistics and records
                   - Major tournaments and championships
            
                   Guidelines:
                   1. Provide accurate, up-to-date information about sports
                   2. When discussing statistics or records, include:
                      - Relevant timeframes
                      - Official sources where available
                   3. For comparison questions, be objective and cite measurable criteria
                   4. If uncertain about any information, respond with:
                      "I don't have definitive information about that. Would you like me to look up recent stats?"
                   5. Never invent or speculate about facts
            
                   Response format preferences:
                   - Use bullet points for lists
                   - Include dates/years for historical context
                   - Highlight exceptional achievements with ★
                   - Maintain neutral, professional tone
            
                   Sports domains covered:
                   • Football (Soccer) • Basketball • Tennis
                   • American Football • Baseball • Cricket
                   • Olympics • Athletics • More upon request
            """;
    // Celebrity Prompt (loaded from file)
    public static final String CELEB_PROMPT_TEMPLATE = """
            List the details of {name}
            along with their career achievements.
            Show the details in this format:
            
            1. Personal Background
            2. Career Timeline
            3. Major Accomplishments
            4. Cultural Impact
            """;
    // Sports User Prompt Template
    public static final String PLAYER_USER_PROMPT_TEMPLATE = """
            List the professional career details and major achievements of %s (the sports player/team).\\s
                                                                             Present the information in this structured format:
            
                                                                             1. Basic Information:
                                                                                - Full Name:\\s
                                                                                - Sport/Discipline:\\s
                                                                                - Nationality:\\s
                                                                               - Active Years:\\\\s
            
                                                                                                                                               2. Career Highlights:
                                                                                                                                                  - Major Championships/Titles won
                                                                                                                                                  - Records held
                                                                                                                                                  - Notable performances/awards
            
                                                                                                                                               3. Key Statistics:
                                                                                                                                                  - [Sport-specific metrics, e.g., goals scored, average points, race times]
            
                                                                                                                                               4. Legacy/Impact:
                                                                                                                                                  - How they influenced their sport
                                                                                                                                                  - Unique contributions or innovations
            
                                                                                                                                               Make the response clear, well-organized, and focused on significant accomplishments.
                                                                                                                                               Avoid trivial details and maintain factual accuracy.
            """;

    private PromptConstants() {
    }

}
