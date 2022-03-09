# MiNET Test UI

## Project Setup:

1. Download the source code from Github
2. Open Android Studio and Open Project (need to open the src folder and not the root folder)
3. Build the project and make sure to configure SDK properly to build project
4. Once Project is built you will now be able to push to MiNET Test UI to the **MiDEVICE**

## Commands for managing ADB for the MiDEVICE:

- Connect to device - `adb connect [IP_ADDRESS]`
- Disconnect from device - `adb disconnect [IP_ADDRESS]`
- List devices - `adb devices`
- Shell into device - `adb shell`
- Kill adb server - `adb kill-server`
- Push files to device - `adb push [LOCAL_PC_FILE_DESTINATION] [DEVICE_FILE_DESTINATION]`
- Pull files from device - `adb push [DEVICE_FILE_DESTINATION] [LOCAL_PC_FILE_DESTINATION]`
- Install apk on device - `adb install -r [LOCAL_PC_APK_DESTINATION]`

## Building and running MiNET UI on midevice:

1. Connect to device using adb `connect` command, and make sure android studio shows device.
2. Once connected and device shows in Android Studio, you can now click the play button to push service to device.
3. Once you have clicked the play button, the project will build and push apk to device, then the application should auto start up.

## Generating APK for production:

1. Make sure to build project before hand.
2. Select **Build** -> **Generate Signed Bundle / APK**
3. Then select the **APK** option and go next
4. Under the **Key Store Path**, select **Choose Existing** to locate the midevice keystore file.
5. Once selected the keystore file, it will auto fill in the rest of the details. Then select next
6. Select release build on the next page.
7. Click **Finish**
8. Locate the build .apk file in the release build folder of project structure in a file explorer. File Destination **MiTestUI** -> **src** -> **app** -> **release**
9. Once .apk is found, it needs to placed on the FTP Server in the following Directory. www.minet.co.za/midevice/bus/RefreshedLatestUI
10. Delete old refreshedui.apk and replace it with new refreshedui.apk (make sure that the uploaded file is named exactly `refreshedui.apk`)
11. Change the version number in the version.txt to match that of the new refreshedui.apk
