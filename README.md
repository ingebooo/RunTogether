# RunTogether
In order to run the wearable from Android Studio (from mac), access to adb through terminal is necessary.
Run this command from terminal: export PATH="/Users/myuser/Library/Android/sdk/platform-tools":$PATH 
change "myuser" with correct username.

Some issues related to debugging can occur if using Moto 360
if so, try 
adb forward tcp:4444 localabstract:/adb-hub
and
adb connect 127.0.0.1:4444

Connect the handheld, and wearable debugging should work.

//Firebase
In order to use the backend, create a firebase database and change refrence in Config.java
