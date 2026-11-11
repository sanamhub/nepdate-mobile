The month bar drew Settings, Previous month, Today and Next month in one trailing group. Four items do not fit beside an inline title: iOS moved Today and Next month into the bar's More menu, so Next month took two taps on every month except the current one.

Today now sits in a leading group of its own, where the bar is empty, and the paging pair stays together on the trailing side. No icons, no smaller type, and Settings stays where the shell puts it. Next month is one tap on every month.

The UI tests assert that Previous month, Next month and Today are each in the bar and hittable on a month away from today at both ends of the range, that one tap on Next month opens the next month, that Today returns to today's month, and that the bar's buttons need no more width than the smallest supported phone has. The screenshots of the bar on today's month and on a month away, at the default text size and at accessibility2, are attached to the test run and uploaded with the run.

## Run

Xcode ci passed on this head. The bar screenshots are in the run's `ios-test-attachments` artifact.
