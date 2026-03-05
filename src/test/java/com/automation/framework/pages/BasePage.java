package com.automation.framework.pages;

import com.microsoft.playwright.Page;
import io.qameta.allure.Step;

public abstract class BasePage {
    protected final Page page;

    protected BasePage(Page page) {
        this.page = page;
    }

    @Step("Open relative path: {relativePath}")
    public void open(String relativePath) {
        page.navigate(relativePath);
    }

    @Step("Read current page title")
    public String title() {
        return page.title();
    }
}
