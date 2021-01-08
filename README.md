![logo](media/logo.png)

RNArtist allows you to design your RNA 2D structures interactively. To help you to be an RNArtist, this tool provides numerous graphical options to find your theme and to modify the 2D layout.

![Screen Capture](media/Capture%20d’écran%202020-12-27%20à%2020.48.24.png)

## Prerequisites
You need the tool maven and a Java distribution to be installed (type the commands ```mvn``` and ```java``` from a command line to check). RNArtist has been developped with OpenJDK 15. To compile it with Java 11, you need to change the value of the property java.version in the pom file.

## Installation

Download the projectPanel as [a zip file](https://github.com/fjossinet/RNArtist/archive/master.zip) or with the command git (git clone https://github.com/fjossinet/RNArtist.git).

From the projectPanel directory, type: 

```
mvn clean package
```

## Launch

### Using Maven

From the projectPanel directory, type:

```
mvn exec:exec
```

### Using the launch scripts directly

From the subdirectory target/RNArtist, run the file for your operating system by typing: 

```./launch_rnartist_for_...```

You can find more details about this projectPanel on my [Twitter Account](https://twitter.com/rnartist_app)
