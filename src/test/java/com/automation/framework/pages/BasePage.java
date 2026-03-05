package com.automation.framework.pages;

import com.microsoft.playwright.Page;

public abstract class BasePage {
    protected final Page page;

    protected BasePage(Page page) {
        this.page = page;
    }

    public void open(String relativePath) {
        page.navigate(relativePath);
    }

    public String title() {
        return page.title();
    }
}
