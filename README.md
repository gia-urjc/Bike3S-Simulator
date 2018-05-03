# Bike3S - A Bike Sharing System Simulator

## What is B3S
Bike3S is a simulator created with the idea of test differents situations in real bike sharing systems. 
Bike sharing systems allow to citizens move between different places. Bike3S give us the posibility to
simulate and recreate situations, use different types of users, try algorithms and use recomendation
systems... 

The motivation of this project is to search ways to rebalance the system with recomendations and incentives.

 
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

![It shows the architecture of the software. It shows two clear parts: backend and frontend](documentation/images/Arquitecture_6.png?raw=true "Software arquitecture")

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

## Backend
[Backend Setup - IntelliJ IDEA](documentation/backend_setup_intellij.md)

[Backend Setup - Eclipse](documentation/backend_setup_eclipse.md)

## Frontend
[Frontend Setup - Webstorm](documentation/frontend_setup_webstorm.md)

[Frontend Setup - VScode](documentation/frontend_setup_vscode.md)

## Build From Command Line
If you want to use any text editor, you can. It's not necessary any IDE to compile, build or distribute this project.

# Distribute
To create a distributable package of the software run `npm run distribute` from the project root. It will compile and
bundle all the parts together.

# Fundamentals
