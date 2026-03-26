# Open Plural
> This repository now includes a native iOS app port under `ios/OpenPluralIOS`.

## iOS app
The iOS source lives in `ios/OpenPluralIOS/OpenPluralIOS` and mirrors the working Android feature set:

- login and registration
- local persisted session and offline snapshot
- dashboard routing
- members and folders
- fronting controls
- friends
- account settings
- app options

Open the Xcode project at `ios/OpenPluralIOS/OpenPluralIOS.xcodeproj`.

Important:
- Full iOS builds require full Xcode, not just Command Line Tools.
- Several dashboard sections were already unimplemented in Android. The iOS port keeps those routes visible as placeholders instead of inventing missing backend behavior.

## Android app
The original Android Studio project remains in place under `app/` as the source reference for the iOS migration.

## Connecting to the server:
To use a local backend, click the "Please log into your account" text on the login screen 10 times.  
This shows you a third field, where you can enter a custom base url.  
Make sure to include the "http" or "https" protocol and to not include a final slash!

## Additional Developer Debug Screen:
You can enable "Developer mode" in the Options screen.  
This shows you "Developer" button on the dashboard, that provides you a list of buttons to execute actions.  
To view their implementations, please check screens/Developer.kt
