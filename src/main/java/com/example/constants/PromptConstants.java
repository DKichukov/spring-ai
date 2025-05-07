package com.example.constants;

public final class PromptConstants {
    // Sports System Prompt Template
    public static final String PLAYER_USER_PROMPT_TEMPLATE = """
        Provide details for %s including:
        - Full official name
        - 3-5 most significant career achievements
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
    public static final String PLAYER_SYSTEM_PROMPT = """
        You are a sports data specialist. Always respond with:
        - Player's full name
        - List of their top 5 achievements
        Format as pure JSON without Markdown:
        {
          "playerName": "Full Name",
          "achievements": [
            "Achievement 1",
            "Achievement 2"
          ]
        }
        """;

    public static final String PLAYER_ACHIEVEMENT = """
    Provide a list of achievements for {player}.
    """;

    private PromptConstants() {
    }

}
