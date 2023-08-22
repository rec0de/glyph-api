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