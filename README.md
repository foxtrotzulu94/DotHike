# .Hike Android Application
### ECE 390 Team Design Project

## Overview
".Hike" is an Android Application for hiking enthusiasts which can be used to track environmental conditions, location and path during outdoor activities.
The current version of the application relies on the use of the Texas Instruments SensorTag for retrieving environmental data and presenting it to the user.

## Building and Running
The minimum Android API version is 19 (KitKat 4.4.4) and gradle is required to build successfully. The application includes several libraries, marked as dependencies in the gradle.build file. You will be notified of any necessary additional installations you might need to make.
Once a build is running, you'll also want to register with Google as a developer to get a Google Maps API Key to be able to access and view maps on the application.

There is also a small test-suite included in the source files which verify that the backend data structure and logic is running correctly. If you make any modifications to the "dotprod.data" package, run this test-suite to make sure everything works as expected. The test suite must be run on an Android Device, physical or virtual.

## Modificating and Extending
The application was created following standard MVC architecture and is made up of 3 main packages: `data`, `hw` and `loc`. Each of these has a main class that provides essential functionality to the application. Thus, the packages can be modified extensively, provided that they maintain the same set of basic features. Read through the code or generate the JavaDocs document to read up on more.

Summary of the Packages/Classes depending on what you want to modify:
* `data` and *Hike Data Director*: Anything related with DB Access, user profile, statistics collection or data management
* `hw` and *Hike Hardware Manager*: Modify/extend if you want to add more sensors to the application.
* `loc` and *Hike Location Entity*: Handles location filtering and updates, could be changed to replace Google Location service

## Credits
### The Team
* Eric Tremblay
* Javier Fajardo

## Legal Notice
Two important notes must be made regarding the code in this repo

1. The ".Hike" application includes the SensorTagLib, provided as is by Texas Instruments through the Electrical and Computer Engineering Department of Concordia University. This library and all of its components are owned by Texas Instruments and the authors of .Hike make no claims of ownership. Please refer to the [Limited License](sensorTagLib/LICENSE.MD) for more info of use.

2. The original authors of the application have claimed ownership over the source code (minus external libraries used) in the [Main project folder](/app) under the [Apache 2.0 License](LICENSE.MD). In short, whoever uses this code  is **responsible for abiding the academic code of conduct of the institution they are enrolled in and, respecting such, must accept full responsibility of what they made do with this code, freeing the original authors from any consequences that may derive from such actions.**
