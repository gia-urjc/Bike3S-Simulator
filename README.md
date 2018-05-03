# Bike3S - A Bike Sharing System Simulator

## What is B3S
Bike3S is a simulator created with the idea of test differents situations in real bike sharing systems. 
Bike sharing systems allow to citizens move between different places. Bike3S give us the posibility to
simulate and recreate situations, use different types of users, try algorithms and use recomendation
systems... 

The motivation of this project is search ways to rebalance the system with recomendations and incentives.

 
# Prerequisites
1. JDK 1.8
2. Maven 3.5
3. Node.js 8.9

Please make sure that all the binaries are registered in your PATH.

The package manager NPM is also required but is usually bundled with the Node.js installer.

# Getting Started for Development 
This project is development environment agnostic. You can use an IDE or just the command line.


## General overview of the software architecture
The project is separated in two principal parts, a **backend** and a **frontend**.

***Image of the architecture***

The **backend** is related with all the simulation logic. Backend is implemented in Java.

The folder `/backend-bikesurbanfleets` contains this part of the project

The **frontend** is related with all the GUI and data analysis of the simulations. It is implemented in TypeScript,
Angular, and use Electron.

The folder `/frontend-bikesurbanfleets` contains this part of the project.

## Setup
This project is development environment agnostic. You can use an IDE or just the command line.

The easiest option is to use the Ultimate Edition of IDEA since it is the only IDE from IntelliJ that allows having
modules of different technologies in one project. Alternatively the free community edition of IDEA can be used to
develop the Java part in IDEA and the frontend might be developed in a free webdevelopment-tailored editor like Atom or
VS Code.

1. First of all, be sure you have all the [Prerequisites](#prerequisites) installed and working in your system.
2. Execute this in the project directory.
```
npm install && node fuse configure:dev
```

Below are recommended setups for common IDEs for **backend** and **frontend**.

### Backend - IntelliJ IDEA


### Backend - Eclipse


### Frontend Setup 
The easiest option is to use the Ultimate Edition of IDEA since it is the only IDE from IntelliJ that allows having
modules of different technologies in one project. Alternatively the free community edition of IDEA can be used to
develop the Java part in IDEA and the frontend might be developed in a free webdevelopment-tailored editor like Atom or
VS Code. The following steps show the recommended setup in IDEA Ultimate.

1. Clone this repository from within IDEA
2. If asked, confirm to create a project and click through the setup to accept all the default settings. This point
   might be omitted, depending from which menu the cloning took place.
3. When the project is ready go to `File` &rarr; `Project Structure...`.
4. Under `Project` set the Project SDK to 1.8 and the Project language level to 8.
5. Under `Modules` delete whatever modules IDEA might have setup automatically.
6. Add a New Module and choose Static Web from the list. Make sure that both the Content Root and Module File Location
   are set to the project root.
7. Import a module and select the folder `./backend-bikesurbanfloats`. Next choose to import from external model and
   select Maven. On the Maven configuration page check the box to automatically import the maven project and leave the
   rest as it is. That's all for the project structure configuration.
8. To run and debug the Java part from IDEA we need to setup a run configuration. Right click on
   `com.urjc.iagroup.bikesurbanfloats.Application` and select to run the `main()` method. This will automatically
   generate a run configuration. The first run will fail and we will have to modify the generated run configuration a
   little. Go to `Run` &rarr; `Edit configurations...`. Our generated configuration should be the only one available and
   already be selected. Add the path to a valid `configuration.json` as the program argument and append
   `\backend-bikesurbanfloats` to the working directory. That's it.
9. Lastly, open the Terminal Tool Window and run `npm install` to download all the JavaScript dependencies.

## Eclipse
An alternative way to develop the Java part `backend-bikesurbanfloats` is to use the Eclipse IDE. Other technologies, 
`frontend-bikesurbanfloats`, might be developed in a webdevelopment-tailored editor like Atom or VS Code. The following
steps show how to configure the project in Eclipse.

1. You can clone the project from Eclipse but we recommend clone it directly from terminal using git `git clone repository-url` 
at the folder you want.
2. Go to  `File` &rarr; `Import` &rarr; `Maven` &rarr; `Existing Maven Project`.
3. Click on `Browse` and select in your project directory, the folder `backend-bikesurbanfloats`
4. Then, the import wizard should detect a `pom.xml`. Select the `/pom.xml` checkbox and click on Finish.
5. All dependencies shoulb be downloaded now to your Eclipse project and Git repository is automatically synchronized to the remote
repository.
6. To run and debug the project correctly, go to `Run` &rarr; `Run Configurations`. Now go to the `Arguments` section and add into 
`Program arguments` the next arguments:
```
-schema "SCHEMA_CONFIGURATION_PATH" -config "CONFIG_JSON_PATH" -validator "VALIDATOR_JSON_PATH"
``` 
7. Click on `Apply` and `Run`.

# Distribute
To create a distributable package of the software run `npm run distribute` from the project root. It will compile and
bundle all the parts together.

# Documentation
TODO
