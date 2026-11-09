# ADR-0013: Opt-in date notification on Android

- Status: Accepted
- Date: 2026-11-08
- Deciders: Sanam Pakuwal
- Amends: ADR-0007, decision 2 ("No permissions requested in v1 ... No notifications")

## Context

Testers asked for today's date at the top of the screen, the way other Nepali calendar apps show
it: the day number as a status bar icon, and a card with the full date in the notification shade.
Android draws a status bar icon only for a posted notification, and from Android 13 an app must
hold the runtime permission `POST_NOTIFICATIONS` to post one. ADR-0007 allows no runtime
permission and no notifications in v1, so this feature needs a decision of its own.

## Decision

1. Two settings, both off by default, in Settings, App:
   - "Day in status bar": today's BS day drawn as the notification's small icon, on a
     low-importance channel (no sound, no pop-up, icon shown). With only this switch on, the
     notification is one line, because Android has no icon without a notification.
   - "Date in notifications": a silent card with the weekday, BS date, tithi, AD date and today's
     holiday. With only this switch on, the card uses a minimum-importance channel, which Android
     keeps out of the status bar.
2. The app asks for `POST_NOTIFICATIONS` only when a switch is turned on, never at start. A refusal
   leaves the switch off; nothing else changes.
3. The notification is drawn from the same snapshot as the widgets and is refreshed by the same
   paths (ADR-0006): the midnight alarm, boot, time, zone and locale changes, settings changes and
   app open. No service, no WorkManager, no exact alarm.
4. `checkPermissions` allows `android.permission.POST_NOTIFICATIONS` next to
   `RECEIVE_BOOT_COMPLETED`. Everything else in ADR-0007 stands: no network, no SDKs that collect
   data, store declarations unchanged.

## Consequences

- The privacy promise holds: the notification is drawn on the device from bundled data and sends
  nothing anywhere.
- The Play listing and the privacy policy must mention the optional notification permission.
- A person who never turns a switch on sees no permission prompt and no notification.
- If this ADR is rejected, the two switches, `DateNotification` and the manifest entry are removed
  and ADR-0007 applies unchanged.
