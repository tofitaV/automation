package com.automation.framework.base;

import com.microsoft.playwright.Page;
import io.qameta.allure.Allure;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.ByteArrayInputStream;
import java.util.Base64;

public class AllureListener implements ITestListener {

    @Override
    public void onTestFailure(ITestResult result) {
        Page page = BaseTest.currentPage();
        if (page == null || page.isClosed()) {
            return;
        }

        try {
            byte[] buffer = page.screenshot();
            Allure.addAttachment(
                    "Failure screenshot",
                    "image/png",
                    new ByteArrayInputStream(buffer),
                    ".png"
            );
        } catch (Exception exception) {
            Allure.addAttachment("Failure screenshot error", exception.getMessage());
        }
    }
}
