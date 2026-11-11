# Export compliance

`ITSAppUsesNonExemptEncryption` is `false` in `ios/Config/NepDateApp-Info.plist`. The app uses no
encryption of its own: it makes no network request, stores settings in the App Group, and uses only
the operating system's standard facilities. It does not include a proprietary or non-standard
cryptographic implementation.

The answer in App Store Connect is **No** to "Does your app use non-exempt encryption?".

If a future release adds a network feature, this answer and the plist key must be reviewed together
with the privacy policy and the network gate in `ios/scripts/check-network.sh`.
