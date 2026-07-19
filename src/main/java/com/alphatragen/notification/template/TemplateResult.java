package com.alphatragen.notification.template;

public class TemplateResult {
    private final String title;
    private final String content;

    public TemplateResult(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}
