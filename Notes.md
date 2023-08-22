`executeStartProgressAnimation()` only triggers once thanks to the `BasePackageNotificationController.startedProgressAnimation` variable

This variable gets set to `false` in the `BasePackageNotificationController.reset()` method. It also is set via the `setStartedProgressAnimation()` method.

`GlyphNotificationManager` manages state of the currently active controller via `mActivieController`. `reset()` is called inside `GlyphNotificationManager.finishOrStartAnotherGlyph()`. This is called in `finishCurrentGlyph()`

There are a few places in `GlyphNotificationManager` where `finishCurrentGlyph()` gets called
- `finishActivieContrllerIsIfNeeded()` 
- `updateGlyphResult()`, based on the `BasePackageNotificationController.getTargetStop()` value
- `cancelAction()`

### `BasePackageNotificationController.getTargetStop()`

`targetStop` seems to be set as soon as `int onNotificationPosted` that's returned by `findTargetParser.onNotificationPosted()` in `BaseNotificationController` returns a -1. 

`UberPickupTimeParserRule` never returns a -1. Only returns a 0 or 1. This is why the controller does not get reset.

`UberCancelParserRule` returns a -1 under some cases. I can use this to turn off the LEDs I guess.


## Cancelling an animation

### Attempting to spoof notifs that match `UberCancelParserRule`

- Notification `title` can't be null. 
- Title should be `Cancel Trip`. 

This works perfectly. I believe it can be used to reset any animation. I just had to remember to add the string from Uber's `strings.xml` which contains the `Cancel Trip` text to my own app's `strings.xml`


## Hiding notifications from user

### Attempting to cancel notification immediately sending it
This does not trigger any LED animation update.

### Attempting to cancel notification with a small delay
Very hacky approach, but this sort of works. With a delay like 100ms, the notification does not seem to be detected by the `GlyphNotificationListenerService`. Howevever 1000ms works, but the notification icon pops up in the status bar momentarily. 

200ms seems like a magic number where the notification doesn't pop up in the status bar but still gets recognized by `GlyphNotificationListenerService`.