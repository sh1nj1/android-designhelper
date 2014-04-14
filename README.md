android-designhelper
====================

This is Utility app helps you to compare and measure designed screen by designer with your real activity.
This app will be run on top of you app transparently so that you can compare beneath screen with design screen.

This is very first implementation of this idea.

# Get started

Upload .png, .jpg(.jpeg) files to /sdcard/DesignHelper_Assets folder on the device.

```
adb shell mkdir /sdcard/DesignHelper_Assets
adb push ./design_screens/ /sdcard/DesignHelper_Assets/
```

Run your activity for the design.
And run Android DesignHelper on the top of the Activity.

To run the DesignHelper Activity:

```
adb shell am start -n "com.sh1nj1.android.designhelper/com.sh1nj1.android.designhelper.DesignScreenActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER
```

After installed if the DesignHelper App goes background it will issue notification to relaunch the app on top of the previous app.

# Controlls

All coordinates are using dip unit.

## Top controls

* "Ti" button - is reduce the DesignHelper Activity on the center so that you can control beneath Activity to change the beneath activity.
Left-Top corner there are two boxes bordered black color, it can be draggable so that you can measure two boxes' distance.
* Seekbar - change the alpha property of the DesignHelper Activity.
* Combobox - reload image in different scale mode.

## Bottom Controls

* "0" - init Design Screen location to x=0, y=0
* "<<" - load previous design screen image.
* ">>" - load next design screen image.
* "<" - move last dragged object to left by 1 px.
* "^" - move last dragged object to top by 1 px.
* "." - move last dragged object to bottom by 1px.
* ">" - move last dragged object to right by 1px.