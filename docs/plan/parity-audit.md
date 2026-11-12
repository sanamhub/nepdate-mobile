# Parity audit

Status of every requirement, widget and interactive surface on Android and iOS. Code and test
names are the evidence. A row marked **manual** is a device or owner check, not a code gap.

Legend: **tested** means code plus an automated test; **code only** means the code exists without
an automated test; **manual** means a row in `docs/runbooks/manual-test.md`; **n/a** means the
surface does not exist on that platform; **gate** means a CI script.

| Item | Android | iOS | Evidence | Gap |
|---|---|---|---|---|
| MR-01 Month view | tested | tested | `MonthLayoutTest.asoj2083`, `MonthScreenTest.mr01Ac5NextAndPreviousChangeTheMonth`; `MonthLayoutTests.asoj2083`, `MonthPagingTests.testSwipeMovesOneMonthEachWay` | none |
| MR-02 Day detail | tested | tested | `DayDetailTest.mr02Ac1AndAc5ShowTheDateTithiHolidayAndEventsInOrder`; `DayDetailViewModelTests.asoj9`, `DayDetailUITests.testSheetShowsItsFieldsAndCopiesTheDate` | none |
| MR-03 Upcoming | tested | tested | `UpcomingTest.firstTwentyEntriesStartWithTodaysEvents`; `UpcomingViewModelTests.firstEntries` | none |
| MR-04 Converter | tested | tested | `ConverterTest.invalidDayKeepsThePreviousResult`; `ConverterViewModelTests.invalidDay` | none |
| MR-05 Widgets | tested | tested | `WidgetModelTest.w01HolidayHasTheHolidayNameAndMark`; `WidgetTimelineTests.kathmanduMidnights` | placement and rendering-mode passes are device rows |
| MR-06 Nepal date abroad | tested | tested | `TodayProviderTest.nepalTimeIsTheNextBsDay`; `TodayProviderTests.nepalTime` | none |
| MR-07 Settings | tested | tested | `SettingsScreenTest.choicesWriteTheSettings`; `SettingsStoreTests.roundTrip` | none |
| MR-08 Privacy | gate | gate | `checkNoInternet`, `checkPermissions`; `ios/scripts/check-network.sh` | no runtime network test; CI gates are the evidence |
| MR-09 Performance and size | tested | tested | `MidnightSchedulerTest`, `WidgetTimelinePerformanceTests.testMemory`; `PerformanceUITests` | cold start, frame trace, download size and peak memory are device rows |
| MR-10 Accessibility | tested | tested | `MonthScreenTest.mr10Ac1Asoj25LabelMatchesTheRequirement`; `MonthAccessibilityTests.testGridHasOnlyTheToleratedAuditFindings` | VoiceOver, TalkBack and Switch Access passes are device rows |
| MR-11 Localisation | partial | tested | `values-ne/`, `MonthNamesTest.nepaliNamesAreTheLibraries`; `ci.sh` xcstrings gate | Android has no CI gate for a missing Nepali string |
| MR-12 Holidays in system calendar | tested | tested | `AboutLinksTest.subscriptionShareCarriesTheUrlAsText`, `thePublishedHolidayFeedIsAnAllowedProjectSite`; `SettingsAboutTests.subscriptionURL` | none |
| MR-13 Search | tested | tested | `SearchJumpTest.eightBareDigitsReadAsYearMonthDay`, `SearchScreenTest.searchGroupsKeepOnlyTheNamesThatMatch`, `theLandingStateHasTheDateCardsAndNoRecentChips`; `SearchTests.nameQuery`, `recentSearches`, `landing` | none |
| MR-14 Year view | tested | tested | `YearScreenTest.theYearShowsTwelveMonthsAndTappingOneOpensIt`, `theRangeStartsAtTheFirstYear`; `YearViewModelTests.months`, `MonthAccessibilityTests.testYearViewReachesBothEndsOfTheRange` | none |
| MR-15 First run | tested | tested | `FirstRunTest.aFreshInstallShowsTheFirstRunThenRemembersIt`, `theShellShowsTheFirstRunUntilTheFlagIsSet`; `ShellModelTests.firstRunFlag`, `ShellUITests.testFirstRunShowsOnceAndOpensThePatro` | none |
| MR-16 Appearance | tested | tested | `ThemeTest.changingTheLookRepaintsAtOnce`; `SettingsAboutTests.appearanceAtOnce` | none |
| MR-17 Show AD dates | tested | tested | `MonthSettingsTest.theAdDayIsGoneWhenTheSettingIsOff`; `MonthViewModelTests.adNumberSetting` | none |
| MR-18 Opens on | tested | tested | `SettingsRepositoryTest.displayPreferencesDefaultAndWrite`; `SettingsStoreTests.opensOnTabs` | none |
| MR-19 Haptics | manual | manual | `Haptics.kt`; `Haptics.swift` | haptic feel is a device row |
| MR-20 Day sheet navigation and share | tested | tested | `DayDetailTest.mr20SteppingCrossesTheMonthEndAndStaysInTheSheet`, `DayShareTextTest`; `DayDetailUITests.testNextAndPreviousDayStayInTheSheet`, `DayDetailViewModelTests.shareText` | none |
| MR-21 Converter between dates | tested | tested | `ConverterTest.secondDateGivesYearsMonthsAndDays`; `ConverterViewModelTests.comparison` | none |
| MR-22 Today card | tested | tested | `TodayChipPlanTest`; `TodayWidgetTests.quietDay`, `threeEvents`, `moreThanThree`, `noEvents` | none |
| MR-23 Holidays and events under the grid | tested | tested | `MonthHighlightsTest.everyHolidayAndEventDayHasOneRowInDateOrder`, `aMonthWithoutHolidaysOrEventsDrawsNoBlock`; `MonthViewModelTests.highlightDays` | none |
| W-01 Today | tested | tested | `WidgetModelTest.w01NormalDayHasTheExpectedStrings`; `WidgetTimelineTests.holidayAndNormalDay`, `TodayWidgetTests.gregorianFace` | none |
| W-02 Today and upcoming | tested | tested | `WidgetModelTest.w02ListsTheNextDaysAfterTodayAndUsesTheCoverageFooterAtTheEnd`; `WidgetTimelineTests.entryContent` | none |
| W-03 Month | tested | tested | `WidgetContentTest.w03NavigationCallbacksCarryTheAdjacentMonths`; `MonthWidgetTests.MonthWidgetIntentTests.previousNextToday` | `systemExtraLargePortrait` is not implemented; see the note below |
| W-04 Lock screen | manual | tested | `widget_today_info.xml`; `WidgetSupportTests.LockScreenWidgetModelTests.content` | Android placement is a device row |
| W-05 Control | n/a | tested | `ControlWidgets.swift`; `WidgetControlTests.routes` | gallery placement is a device row |
| W-06 Festival countdown | tested | tested | `WidgetControlsTest.nextFestivalStartsAtTheNearestDayAndStopsAtBothEnds`; `FestivalWindowNavigationTests`, `FestivalWindowTests`, `FestivalGaugeTests` | none after the twelve-day window fix and the gauge |
| W-07 Festivals list | tested | tested | `WidgetControlsTest.theListShowsEventsAndHolidaysByDefaultAndTheFilterKeepsOnlyHolidays`; `TodayWidgetTests.holidaysOnly` | none |
| W-08 Date chip | tested | tested | `NewWidgetsTest.theDateChipShowsTheMonthAndOpensTheApp`; `LockScreenWidgetModelTests.content` | none |
| W-09 Date and tithi | tested | tested | `WidgetContentTest.theDateStripShowsTheWeekdayDateAndTithiInEnglishDigits`; `LockScreenWidgetModelTests.rectangularTithi` | none |
| W-10 This week | tested | tested | `NewWidgetsTest.theWeekRunsSundayToSaturdayAroundToday`; `WidgetWeekTests.week`, `agenda` | none |
| W-11 Date text | code only | tested | `TextDateWidget.kt`; `LockScreenWidgetModelTests.content` | Android one-line face has no test |
| Android Glance: W-03 previous, next, today, day link | tested | n/a | `WidgetContentTest.w03NavigationCallbacksCarryTheAdjacentMonths`, `w03TodayCallbackRestoresTheCurrentMonth`, `w03DayTapCarriesTheDeepLinkExtra` | none |
| Android Glance: W-06 previous and next | tested | n/a | `WidgetControlsTest.theFestivalArrowsCarryTheIndexToShow` | none |
| Android Glance: W-07 holidays-only | tested | n/a | `WidgetControlsTest.aFestivalRowOpensItsDayAndTheChipTogglesTheFilter` | none |
| iOS App Intents: Today flips BS and AD | n/a | tested | `TodayWidgetTests.gregorianFace` | none |
| iOS App Intents: next festival and midnight reset | n/a | tested | `FestivalWindowNavigationTests`, `WidgetRefreshTests` | none |
| iOS App Intents: Month previous, today, next and day tap | n/a | tested | `MonthWidgetTests.MonthWidgetIntentTests`, `dayLinks` | none |
| iOS App Intents: Festivals holidays-only | n/a | tested | `TodayWidgetTests.holidaysOnly` | none |
| Android Quick Settings tile | tested | n/a | `TodayTileService.kt`, `TodayTileTest` | tile lifecycle on a device is manual |
| Opt-in notification bar (ADR-0013) | tested | n/a | `DateNotification.kt`, `DateNotificationTest` | permission dialog is a device row |
| iOS Lock Screen countdown gauge | n/a | tested | `FestivalCountdownGauge.swift`, `FestivalGaugeTests` | placement is a device row |
| iOS Control Center controls | n/a | tested | `ControlWidgets.swift`, `WidgetControlTests.routes` | placement is a device row |

## iPad and the extra-large month widget

`TARGETED_DEVICE_FAMILY` is `1,2`, so the app and the widget extension build for iPhone and iPad.
`systemExtraLargePortrait` is not added. ADR-0014 keeps CI on `macos-26` with Xcode 26.6: the
measured Xcode 27 run failed the snapshot, audit, pager and Settings gates, so adding an iOS 27
family now would not compile on the gate's SDK and could not be reviewed. The move is recorded in
`docs/adr/0014-ios-ci-on-xcode-27.md` and waits for the migration before April 2027.

## Owner or device

- `systemExtraLargePortrait` and its holiday/event list: waits for the Xcode 27 migration and a
  device review.
- Widget placement, rendering modes, StandBy, VoiceOver and Control Center placement.
- Android lock-screen placement and the no-`showWhenLocked` unlock behaviour.
- MR-09 device numbers: iPhone 12 and A15 cold start, 60 fps swipe trace, download size and widget
  peak memory.
- MR-10 device passes: TalkBack, Switch Access, VoiceOver, largest text size.
- MR-19 haptic feel.
- The Android notification permission dialog.
- I3 release work: Apple Developer account, App Store Connect key, TestFlight, the two legal rows.
