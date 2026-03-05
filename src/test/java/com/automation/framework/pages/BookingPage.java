package com.automation.framework.pages;

import com.automation.framework.base.IStep;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.WaitForSelectorState;
import io.qameta.allure.Step;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public class BookingPage extends BasePage<BookingPage> {
    private static final String BOOK_ROOM_SELECTOR = "#rooms .card-footer a";

    public BookingPage(com.microsoft.playwright.Page page) {
        super(page);
    }

    @Step("{step}")
    public BookingPage step(String step) {
        return this;
    }

    @Step("Open booking home page")
    public BookingPage openHome() {
        open("/");
        return this;
    }

    @Step("Read available room count")
    public int availableRoomCount() {
        waitForRoomCards();
        return page.locator(BOOK_ROOM_SELECTOR).count();
    }

    @Step("Get random available room index")
    public int getRandomAvailableRoomIndex() {
        int availableRoomCount = availableRoomCount();
        assertThat(availableRoomCount)
                .as("At least one room should be available for booking")
                .isGreaterThanOrEqualTo(1);
        return ThreadLocalRandom.current().nextInt(availableRoomCount);
    }

    @Step("Open room by index: {roomIndex}")
    public ReservationPage clickBookThisRoom(int roomIndex) {
        Locator roomCard = page.locator(BOOK_ROOM_SELECTOR).nth(roomIndex);
        roomCard.click();
        return new ReservationPage(page);
    }

    @Step("Open first available room")
    public ReservationPage clickBookFirstAvailableRoom() {
        int availableRoomCount = availableRoomCount();
        assertThat(availableRoomCount)
                .as("At least one room should be available for booking")
                .isGreaterThanOrEqualTo(1);
        return clickBookThisRoom(0);
    }

    private void waitForRoomCards() {
        try {
            page.locator(BOOK_ROOM_SELECTOR).scrollIntoViewIfNeeded();
            Locator firstRoomCard = page.locator(BOOK_ROOM_SELECTOR).first();
            firstRoomCard.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(10_000));
        } catch (PlaywrightException ignore) {
        }
    }

}
