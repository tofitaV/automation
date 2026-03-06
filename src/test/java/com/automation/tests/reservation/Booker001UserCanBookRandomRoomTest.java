package com.automation.tests.reservation;

import com.automation.framework.base.BaseTest;
import com.automation.framework.pages.BookingPage;
import net.datafaker.Faker;
import org.testng.annotations.Test;

public class Booker001UserCanBookRandomRoomTest extends BaseTest {
    private final Faker faker = new Faker();
    private final int stayNights = faker.number().numberBetween(2, 10);
    private final String firstName = faker.name().firstName();
    private final String lastName = faker.name().lastName();
    private final String email = faker.internet().emailAddress();
    private final String phone = faker.numerify("380#########");

    @Test(testName = "Booker001 User can book random room", groups = {"smoke"})
    public void booker001UserCanBookRandomRoom() {
        BookingPage bookingPage = new BookingPage(page())
                .step("Step 1: Open home page")
                .openHome();

        int roomIndex = bookingPage.getRandomAvailableRoomIndex();

        bookingPage
                .step("Step 2: Click book this room")
                .clickBookThisRoom(roomIndex)
                .step("Step 3: Select first available date range")
                .selectFirstAvailableDateRangeOnReservationCalendar(stayNights)
                .verifySelectedNightsCount(stayNights)
                .step("Step 4: Enter guest details and click Book Now")
                .clickReserveNowButton()
                .enterGuestDetails(firstName, lastName, email, phone)
                .verifyEnteredGuestDetails(firstName, lastName, email, phone)
                .clickBookNow()
                .step("Step 5: Verify booking successful message")
                .verifyBookingSuccessfulMessage();
    }

}
