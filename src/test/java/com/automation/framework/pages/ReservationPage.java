package com.automation.framework.pages;

import com.automation.framework.base.IStep;
import com.automation.framework.dtos.UnavailableDateRangeDto;
import com.automation.framework.utils.DragAndDropUtils;
import com.automation.framework.utils.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import io.qameta.allure.Step;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class ReservationPage extends BasePage<ReservationPage> {
    private static final Pattern NIGHTS_COUNT_PATTERN = Pattern.compile("x\\s*(\\d+)\\s*nights", Pattern.CASE_INSENSITIVE);
    private static final Pattern ROOM_ID_PATTERN = Pattern.compile("/reservation/(\\d+)");
    private static final TypeReference<List<UnavailableDateRangeDto>> UNAVAILABLE_DATE_RANGES_TYPE = new TypeReference<>() {
    };
    private static final DateTimeFormatter CALENDAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);
    private static final int MAX_CALENDAR_MONTH_MOVES = 3;
    private static final int SEARCH_WINDOW_DAYS = MAX_CALENDAR_MONTH_MOVES * 31;
    private static final String CALENDAR_DAY_CELL_SELECTOR = ".rbc-month-view .rbc-month-row .rbc-row-bg .rbc-day-bg";
    private static final String CALENDAR_MONTH_LABEL_SELECTOR = ".rbc-toolbar .rbc-toolbar-label";
    private static final String CALENDAR_NEXT_BUTTON_SELECTOR = ".rbc-toolbar button:has-text('Next')";
    private static final String NIGHTS_SUMMARY_SELECTOR = "text=/x\\s*\\d+\\s*nights/i";
    private static final String BOOK_NOW_SELECTOR = "form .btn-primary";
    private static final String FIRST_NAME_INPUT = "input.room-firstname";
    private static final String LAST_NAME_INPUT = "input.room-lastname";
    private static final String EMAIL_INPUT = "input.room-email";
    private static final String PHONE_INPUT = "input.room-phone";

    public ReservationPage(Page page) {
        super(page);
    }

    @Step("{step}")
    public ReservationPage step(String step) {
        return this;
    }

    @Step("Select first available date range for {nightsCount} nights")
    public ReservationPage selectFirstAvailableDateRangeOnReservationCalendar(int nightsCount) {
        assertThat(nightsCount)
                .as("Nights count must be greater than 0")
                .isGreaterThan(0);
        String roomId = readRoomIdFromCurrentUrl();
        List<DateRange> unavailableRanges = readUnavailableDateRanges(roomId);
        LocalDate checkInDate = findFirstAvailableCheckInDate(nightsCount, unavailableRanges);
        assertThat(checkInDate)
                .as("Failed to find a selectable date range for %s nights.", nightsCount)
                .isNotNull();

        LocalDate checkOutDate = checkInDate.plusDays(nightsCount);
        navigateCalendarToMonth(YearMonth.from(checkInDate));
        dragDateRangeOnCalendar(checkInDate, checkOutDate.minusDays(1));
        return this;
    }

    @Step("Verify selected nights count is {expectedNightsCount}")
    public ReservationPage verifySelectedNightsCount(int expectedNightsCount) {
        Long selectedNightsCount = readSelectedNightsCount();
        assertThat(selectedNightsCount)
                .as("Expected selected nights to be %s after calendar drag selection", expectedNightsCount)
                .isEqualTo((long) expectedNightsCount);
        return this;
    }

    @Step("Verify room page is opened")
    public ReservationPage verifyRoomOpened() {
        String roomId = readRoomIdFromCurrentUrl();
        assertThat(roomId)
                .as("Expected opened room URL to contain reservation room id but was: %s", page.url())
                .isNotBlank();
        return this;
    }

    private Long readSelectedNightsCount() {
        Locator nightsSummary = page.locator(NIGHTS_SUMMARY_SELECTOR).first();
        nightsSummary.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        String nightsSummaryText = nightsSummary.innerText().trim();

        Matcher nightsMatcher = NIGHTS_COUNT_PATTERN.matcher(nightsSummaryText);
        if (!nightsMatcher.find()) {
            return null;
        }

        return Long.parseLong(nightsMatcher.group(1));
    }

    private String readRoomIdFromCurrentUrl() {
        Matcher roomIdMatcher = ROOM_ID_PATTERN.matcher(page.url());
        assertThat(roomIdMatcher.find())
                .as("Expected current URL to contain room id but was: %s", page.url())
                .isTrue();
        return roomIdMatcher.group(1);
    }

    private List<DateRange> readUnavailableDateRanges(String roomId) {
        APIResponse response = page.request().get("/api/report/room/" + roomId);
        assertThat(response.ok())
                .as("Failed to load unavailable date ranges for room %s. Status: %s", roomId, response.status())
                .isTrue();

        List<UnavailableDateRangeDto> unavailableDateRangeDtos = JsonUtils.fromJson(
                response.text(),
                UNAVAILABLE_DATE_RANGES_TYPE,
                "Failed to parse unavailable date ranges response for room " + roomId
        );

        List<DateRange> unavailableDateRanges = new ArrayList<>();
        for (UnavailableDateRangeDto unavailableDateRangeDto : unavailableDateRangeDtos) {
            LocalDate start = unavailableDateRangeDto.getStart();
            LocalDate end = unavailableDateRangeDto.getEnd();
            if (start == null || end == null) {
                continue;
            }
            unavailableDateRanges.add(new DateRange(start, end));
        }
        return unavailableDateRanges;
    }

    private LocalDate findFirstAvailableCheckInDate(int nightsCount, List<DateRange> unavailableRanges) {
        LocalDate firstCandidateDate = LocalDate.now().plusDays(1);
        for (int dayOffset = 0; dayOffset < SEARCH_WINDOW_DAYS; dayOffset++) {
            LocalDate checkInDate = firstCandidateDate.plusDays(dayOffset);
            LocalDate checkOutDate = checkInDate.plusDays(nightsCount);
            if (isRangeAvailable(checkInDate, checkOutDate, unavailableRanges)) {
                return checkInDate;
            }
        }
        return null;
    }

    private boolean isRangeAvailable(LocalDate checkInDate, LocalDate checkOutDate, List<DateRange> unavailableRanges) {
        for (DateRange unavailableRange : unavailableRanges) {
            boolean overlapsUnavailableRange = checkInDate.isBefore(unavailableRange.endExclusive)
                    && checkOutDate.isAfter(unavailableRange.startInclusive);
            if (overlapsUnavailableRange) {
                return false;
            }
        }
        return true;
    }

    private void navigateCalendarToMonth(YearMonth targetMonth) {
        for (int monthOffset = 0; monthOffset < MAX_CALENDAR_MONTH_MOVES; monthOffset++) {
            YearMonth visibleMonth = readVisibleCalendarMonth();
            if (visibleMonth.equals(targetMonth)) {
                return;
            }

            assertThat(visibleMonth.isBefore(targetMonth))
                    .as("Visible calendar month %s is after target month %s", visibleMonth, targetMonth)
                    .isTrue();
            page.locator(CALENDAR_NEXT_BUTTON_SELECTOR).first().click();
        }

        YearMonth visibleMonth = readVisibleCalendarMonth();
        assertThat(visibleMonth)
                .as("Failed to reach target month %s within %s moves", targetMonth, MAX_CALENDAR_MONTH_MOVES)
                .isEqualTo(targetMonth);
    }

    private YearMonth readVisibleCalendarMonth() {
        Locator monthLabel = page.locator(CALENDAR_MONTH_LABEL_SELECTOR).first();
        monthLabel.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        String visibleMonthText = monthLabel.innerText().trim();
        return YearMonth.parse(visibleMonthText, CALENDAR_MONTH_FORMATTER);
    }

    private void dragDateRangeOnCalendar(LocalDate checkInDate, LocalDate checkOutDateInclusive) {
        Locator dayCells = page.locator(CALENDAR_DAY_CELL_SELECTOR);
        dayCells.first().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

        YearMonth visibleMonth = readVisibleCalendarMonth();
        LocalDate firstVisibleDate = firstVisibleDateOfMonthGrid(visibleMonth);
        int startIndex = (int) ChronoUnit.DAYS.between(firstVisibleDate, checkInDate);
        int endIndex = (int) ChronoUnit.DAYS.between(firstVisibleDate, checkOutDateInclusive);

        Locator startCell = dayCells.nth(startIndex);
        Locator endCell = dayCells.nth(endIndex);
        DragAndDropUtils.dragBetweenCells(page, startCell, endCell, 12);
    }

    private LocalDate firstVisibleDateOfMonthGrid(YearMonth visibleMonth) {
        LocalDate firstDayOfMonth = visibleMonth.atDay(1);
        long daysToPreviousSunday = firstDayOfMonth.getDayOfWeek().getValue() % 7L;
        return firstDayOfMonth.minusDays(daysToPreviousSunday);
    }

    @Step("Click Reserve Now")
    public ReservationPage clickReserveNowButton() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Reserve Now")).click();
        return this;
    }

    @Step("Enter guest details for {firstName} {lastName}")
    public ReservationPage enterGuestDetails(String firstName, String lastName, String email, String phone) {
        page.locator(FIRST_NAME_INPUT).fill(firstName);
        page.locator(LAST_NAME_INPUT).fill(lastName);
        page.locator(EMAIL_INPUT).fill(email);
        page.locator(PHONE_INPUT).fill(phone);
        return this;
    }

    @Step("Verify entered guest details")
    public ReservationPage verifyEnteredGuestDetails(String firstName, String lastName, String email, String phone) {
        assertThat(page.locator(FIRST_NAME_INPUT).inputValue()).isEqualTo(firstName);
        assertThat(page.locator(LAST_NAME_INPUT).inputValue()).isEqualTo(lastName);
        assertThat(page.locator(EMAIL_INPUT).inputValue()).isEqualTo(email);
        assertThat(page.locator(PHONE_INPUT).inputValue()).isEqualTo(phone);
        return this;
    }

    @Step("Click Book Now")
    public ReservationPage clickBookNow() {
        page.locator(BOOK_NOW_SELECTOR).first().click();
        return this;
    }

    @Step("Verify booking successful message is shown")
    public ReservationPage verifyBookingSuccessfulMessage() {
        Locator successMessage = page.getByText("Booking Confirmed");

        String confirmationMessage = successMessage.innerText().trim();
        assertThat(confirmationMessage)
                .as("Expected booking successful confirmation message but was: %s", confirmationMessage)
                .contains("Booking Confirmed");
        return this;
    }

    private static final class DateRange {
        private final LocalDate startInclusive;
        private final LocalDate endExclusive;

        private DateRange(LocalDate startInclusive, LocalDate endExclusive) {
            this.startInclusive = startInclusive;
            this.endExclusive = endExclusive;
        }
    }

}
