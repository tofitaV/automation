package com.automation.framework.factory;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;

public final class BrowserFactory {
    private BrowserFactory() {
    }

    public static Browser createBrowser(Playwright playwright, String browserName, boolean headless, int slowMoMillis) {
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(headless)
                .setSlowMo(slowMoMillis);

        return switch (browserName.toLowerCase()) {
            case "firefox" -> playwright.firefox().launch(launchOptions);
            case "webkit" -> playwright.webkit().launch(launchOptions);
            case "chromium" -> playwright.chromium().launch(launchOptions);
            default -> throw new IllegalArgumentException("Unsupported browser: " + browserName);
        };
    }
}
