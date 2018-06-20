# Bike3S - A Bike Sharing System Simulator

## What is Bike3S
Bike3S is a simulator created for the purpose of testing different behaviours in real bike sharing systems. 
Bike sharing systems allow citizens to move between different places in an simple and economical way. Bike3S offers us the posibility to
execute situations using different infraestructure confifurations, user models and balancing algorithms.
 

The motivation of this project is to search strategies to balance the system resources using systems, typically, based on incentives, which recommend the users to rent or return a bike in a certain station to contribute to the system balancing. 


 
# Prerequisites
1. JDK 1.8
2. Maven 3.5
3. Node.js 8.9

Please make sure that all the binaries are registered in your PATH.

The package manager NPM is also required but is usually bundled with the Node.js installer.

# Getting Started for Development 
This project is development environment agnostic. You can use an IDE or just the command line.


## General overview of the software architecture
The project is separated in two main parts: a **backend** and a **frontend**.

![It shows the architecture of the software. It shows two clear parts: backend and frontend](documentation/images/Arquitecture_6.png?raw=true "Software arquitecture")

The **backend** is related to all the simulation logic and is implemented in Java.

The folder `/backend-bikesurbanfleets` contains this part of the project.

The **frontend** is related to all the GUI and data analysis of the simulations. It is implemented in TypeScript, using
Angular and Electron.

The folder `/frontend-bikesurbanfleets` contains this part of the project.

## Setup

1. First of all, be sure you have all the [Prerequisites](#prerequisites) installed and working in your system.
2. Execute this in the project directory.
```
npm install && node fuse configure:dev
```

Below are recommended setups for common IDEs for **backend** and **frontend**.

The easiest option is to use the Ultimate Edition of IDEA since it is the only IDE from IntelliJ that allows having
modules of different technologies in one project. Alternatively the free community edition of IDEA can be used to
develop the Java part in IDEA and the frontend might be developed in a free webdevelopment-tailored editor like Atom or
VS Code.

## Backend

[Backend Setup - IntelliJ IDEA](documentation/backend_setup_intellij.md)
<!---
[Backend Setup - Eclipse](documentation/backend_setup_eclipse.md)

## Frontend
[Frontend Setup - Webstorm](documentation/frontend_setup_webstorm.md)

[Frontend Setup - VScode](documentation/frontend_setup_vscode.md)
-->
## Build From Command Line
To build the backend execute:
```
node fuse build:dev-backend
```
To build the frontend and execute the GUI:
```
node fuse build:frontend
```

To build all the project:
```
node fuse build:dist
```

# Distribute
To distribute an executable or installer for your OS, just run
```
npm run distribute
```
Executables are generated in `build/dist/`

# Fundamentals
