package com.automation.framework.base;

import com.automation.framework.config.ConfigManager;
import com.automation.framework.factory.BrowserFactory;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

public class BaseTest {
    private static final ThreadLocal<Playwright> PLAYWRIGHT_THREAD = new ThreadLocal<>();
    private static final ThreadLocal<Browser> BROWSER_THREAD = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> CONTEXT_THREAD = new ThreadLocal<>();
    private static final ThreadLocal<Page> PAGE_THREAD = new ThreadLocal<>();

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        Playwright playwright = Playwright.create();
        PLAYWRIGHT_THREAD.set(playwright);

        Browser browser = BrowserFactory.createBrowser(
                playwright,
                ConfigManager.getRequired("browser"),
                ConfigManager.getBoolean("headless", true),
                ConfigManager.getInt("slowMoMillis", 0)
        );

        BROWSER_THREAD.set(browser);

        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                .setBaseURL(ConfigManager.getRequired("baseUrl"))
                .setViewportSize(
                        ConfigManager.getInt("viewportWidth", 1280),
                        ConfigManager.getInt("viewportHeight", 720)
                );

        BrowserContext context = browser.newContext(contextOptions);
        context.setDefaultTimeout(ConfigManager.getInt("defaultTimeoutMs", 10_000));
        CONTEXT_THREAD.set(context);

        Page page = context.newPage();
        PAGE_THREAD.set(page);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        closeIfPresent(PAGE_THREAD.get());
        closeIfPresent(CONTEXT_THREAD.get());
        closeIfPresent(BROWSER_THREAD.get());
        closeIfPresent(PLAYWRIGHT_THREAD.get());

        PAGE_THREAD.remove();
        CONTEXT_THREAD.remove();
        BROWSER_THREAD.remove();
        PLAYWRIGHT_THREAD.remove();
    }

    protected Page page() {
        return PAGE_THREAD.get();
    }

    protected BrowserContext context() {
        return CONTEXT_THREAD.get();
    }

    static Page currentPage() {
        return PAGE_THREAD.get();
    }

    private void closeIfPresent(AutoCloseable autoCloseable) {
        if (autoCloseable == null) {
            return;
        }

        try {
            autoCloseable.close();
        } catch (Exception exception) {
            throw new RuntimeException("Failed to close automation resource", exception);
        }
    }
}
