#!/bin/sh
rm -rf .idea
./gradlew clean
rm -rf .gradle
rm -rf build
rm -rf */build
rm -rf iosdemo/iosApp.xcworkspace
rm -rf iosdemo/Pods
rm -rf iosdemo/iosApp.xcodeproj/project.xcworkspace
rm -rf iosdemo/iosApp.xcodeproj/xcuserdata
