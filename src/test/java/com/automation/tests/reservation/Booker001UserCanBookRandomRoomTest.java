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

    @Test(testName = "Booker001 User can book random room", groups = {"smoke", "regression"})
    public void booker001UserCanBookRandomRoom() {
        BookingPage bookingPage = new BookingPage(page())
                .openHome();

        int roomIndex = bookingPage.getRandomAvailableRoomIndex();

        bookingPage
                .clickBookThisRoom(roomIndex)
                .selectFirstAvailableDateRangeOnReservationCalendar(stayNights)
                .verifySelectedNightsCount(stayNights)
                .clickReserveNowButton()
                .enterGuestDetails(firstName, lastName, email, phone)
                .verifyEnteredGuestDetails(firstName, lastName, email, phone)
                .clickBookNow()
                .verifyBookingSuccessfulMessage();
    }

}
