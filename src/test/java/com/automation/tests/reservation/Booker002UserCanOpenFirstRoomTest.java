package com.automation.tests.reservation;

import com.automation.framework.base.BaseTest;
import com.automation.framework.pages.BookingPage;
import org.testng.annotations.Test;

public class Booker002UserCanOpenFirstRoomTest extends BaseTest {

    @Test(testName = "Booker002 User can open first room", groups = {"smoke", "regression"})
    public void booker002UserCanOpenFirstRoom() {
        new BookingPage(page())
                .step("Step 1: Open home page")
                .openHome()
                .step("Step 2: Open first available room")
                .clickBookFirstAvailableRoom()
                .verifyRoomOpened();
    }
}
