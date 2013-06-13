iRespond Biometric Identification and HPV Vaccine Tracking
==========================================================

This repository contains all the code required to build the different aspects of our project.

## Biometric Identification Software


First, the Biometric software.

### The Server

The server can be found in the ```iRespondIrisServer``` folder. It is written in C++. Your system must support pthreads and have the boost-devel package installed. You can build the server by going to the ```iRespondIrisServer``` and typing ```make```. The executable will be in the ```bin``` folder and will be names ```irespond```.

To run the server, you simply type ```./irespond [-mv] <port number>```. Run the program with no arguments to see what the options mean.

### The Client

The client consists of three Android projects. Import the following projects into the Android Eclipse environment:

1. ```ftrScanApiAndroidHelperUsbHost```
2. ```ftrWsqAndroidHelper```
3. ```iRespondClient```

All three projects must be set to be libraries, and the ```iRespondClient``` must have the first two listed as libaraies for that project. 

You can then export ```iRespondClient``` as a library through Eclipse, but me sure to include all other necessary code.

Note that another project, ```FtrScanDemoUsbHost```, is also included. This is the original demo source code Futronic sent us for the scanner, and is not used in our project.

## HPV Vaccine Tracking

Next, the HPV Tracker.

### The Server

The server can be found in the ```hpvWebInterface``` folder. It is a Ruby on Rails server. We chose rails because all we needed for the HPV app was a simple database, and enabled that quickly and effeciently.

You can install Rails and run the server using the following steps on the command line (on Linux):

1. ```yum install rubygems``` or ```apt-get install rubygems```, depending on your distro. This will install RubyGems, which is necessary for a rails server.
2. ```gem install rails```, which will install Rails onto your machine.
3. Assuming you are in the project's folder, ```rails server```. You may have to type ```rake db:create``` first.

The server should be running on the local machine on port 3000.

### The Client

The client can be found in the ```HPVVaccineTracker``` folder. It is an Android application.

Import that android project into the same Eclipse workplace as you did the Biometric library.

List the ```iRespondClient``` as a library used by ```HPVVaccineTracker```.

You can then use Eclipse to export an APK or run the application directly on a device.
