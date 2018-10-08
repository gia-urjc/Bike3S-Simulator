[![Build Status](https://travis-ci.org/stimonm/Bike3S.svg?branch=master)](https://travis-ci.org/stimonm/Bike3S)
# Bike3S - A Bike Sharing System Simulator

Bike3S is a simulator created for the purpose of testing different behaviors in real bike sharing systems. 
Bike sharing systems allow citizens to move between different places in an simple and economical way. Bike3S offers us the possibility to
execute situations using different infrastructure configurations, user models and balancing algorithms.
 

The motivation of this project is to search strategies to balance the system resources using systems, typically, based on incentives, which recommend the users to rent or return a bike in a certain station to contribute to the system balancing. 

All the documentation is available here

https://cruizba.github.io/Bike3S-documentation/

# Quick Start Guide

It's recommended to follow the documentation. But if you only want to test the simulator, build, compile and use it, here you have a Quick Start guide with the most important commands and configurations.



## Prerequisites
1. JDK 1.8
2. Maven >= 3.5
3. Node.js >= 8.9
4. Git

Please make sure that all the binaries are registered in your PATH.

The package manager NPM is also required but is usually bundled with the Node.js installer.

## Setup

Clone the git repository and install all dependencies:

```
    git clone https://github.com/stimonm/Bike3S.git
    cd Bike3S
    npm install
    node fuse configure:dev
```

If no errors appeared you have now all prepared for all you want. Run the program, compile simulator, etc... Next sections are just commands to compile and run the system.

## Run the Simulator
Releases are available here: [Releases](https://github.com/stimonm/Bike3S/releases). This releases are not stable yet, so the best way to run the program is execute this command after do the setup

```
node fuse build:frontend
```



----------------

## Basic commands for developers
A full guide for developers is available here
https://cruizba.github.io/Bike3S-documentation/developers_guide/

Here you have a collection of the most commons commands when developing the simulator. 

### Build Backend
```
node fuse build:backend
```

### Build Frontend
```
node fuse build:frontend
```

### Build All
```
node fuse build:dist
```

### Create Installer And Portable for your OS
```
npm run distribute
```

### Simulate from the command line
Are you too lazy to configure your IDE? No problem, you can execute this commands to execute the simulator.

**Generate users**:
```
node fuse gen-users:dev
```

**Simulate**:
```
node fuse simulate:dev
```

The configuration files of the simulation runned via `node fuse gen-users:dev` and `node fuse simulate:dev`, are in the project directory in the folder `/backend-configuration-files`. To test quickly simulations without the GUI, you can edit these configuration files and run these commands. The history will be stored in `/build/history`. 
