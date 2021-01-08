# Divergence Meter
Android divergence meter widget like that in Steins;Gate.

#### Features
- Displaying divergence number in nixie tubes;
- Changing the divergence every 2 hours;
- Balanced random system, that lowers the chance of going to another attractor field;
- Ability to set your own divergence number;
- Notifications, when changing attractor field.

#### Images
Normal view:

![widget preview](app/src/main/res/drawable-nodpi/appwidget_preview.jpg)

With minus:

![minus preview](app/src/main/res/drawable-nodpi/appwidget_minus_preview.jpg)

## Download
https://github.com/retanar/Divergence_Meter/releases

#### Known issues
- For unknown reasons, you may encounter an error "Problem loading widget". 
Fix: delete and add the widget again.
- There is -1.000000 divergence which is displayed as -.000000
- Although the program uses a modified random generator it's still random, so it's possible to switch the attractor field a few times in a row. 
I'm working on a fix for this for the 0.4.0 version.
