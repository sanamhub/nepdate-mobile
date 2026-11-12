# Notes for App Review

NepDate is an offline Bikram Sambat calendar for Nepal and Nepalis abroad. It has no account, no
login, no network access and no purchases.

## How to reach every screen

The app opens on the Patro tab, the month grid. The tab bar holds four tabs:

1. **Patro**: the month grid, the today card, the month's holidays and events under the grid, and
   the year view from the month title menu.
2. **Festivals**: the next public holiday, the coming festivals and holidays grouped by month, the
   search field and the All / Public holidays filter.
3. **Converter**: Bikram Sambat to Gregorian and back, and the difference between two dates.
4. **Search**: the next three festivals, the last five searched names, and a typed BS or AD date.

Settings opens from the gear button on every tab. About is the last row inside Settings. There is
no login, onboarding gate or paywall. The first-run screen appears once and opens the Patro tab.

## Widgets and controls

Long-press the Home Screen, choose Edit, then Add Widget, and search for NepDate. The app offers
Today, Today and upcoming, Month, Next festival, Festivals, This week, and the three Lock Screen
families. The Month widget has Previous, Today and Next buttons, and each day opens that day.
Control Center and the Lock Screen also offer "Open today" and "Open converter" through the
Controls gallery. App Shortcuts are in the Shortcuts app: Open today, Go to date, and Next public
holiday.

The widget data is bundled. Widgets update at local midnight and after a settings change. No
network is used.

## Data and privacy

All holiday, tithi and event data is in the app bundle. Settings stay in the App Group on the
device and are shared only with the app's widget extension. The app makes no network request, has
no analytics or crash SDK, and requests no runtime permission.

"Add to calendar" opens `EKEventEditViewController` with the day's event filled in. The person
confirms or cancels; the app never reads the calendar and never asks for calendar access.

## Review environment

Use any iPhone or iPad on iOS 18 or later. The app is not region-locked and does not need a test
account. Set the device language to Nepali to see the Nepali interface; the in-app language row
opens the system per-app language page.

## Contact

The support URL and the privacy policy URL are in the listing. The source is public at
https://github.com/sanamhub/nepdate-mobile.
