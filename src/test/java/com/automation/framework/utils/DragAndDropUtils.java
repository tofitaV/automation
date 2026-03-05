package com.automation.framework.utils;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Mouse;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.BoundingBox;

public final class DragAndDropUtils {
    private DragAndDropUtils() {
    }

    public static boolean dragBetweenCells(Page page, Locator startCell, Locator endCell, int steps) {
        startCell.scrollIntoViewIfNeeded();
        endCell.scrollIntoViewIfNeeded();

        BoundingBox startBox = startCell.boundingBox();
        BoundingBox endBox = endCell.boundingBox();
        if (startBox == null || endBox == null) {
            return false;
        }

        page.mouse().move(startBox.x + startBox.width / 2, startBox.y + startBox.height / 2);
        page.mouse().down();
        page.mouse().move(
                endBox.x + endBox.width / 2,
                endBox.y + endBox.height / 2,
                new Mouse.MoveOptions().setSteps(steps)
        );
        page.mouse().up();
        return true;
    }
}
