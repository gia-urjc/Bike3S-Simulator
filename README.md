# Prerequisites
1. JDK 1.8
2. Maven 3.5
3. Node.js 8.9

Please make sure that all the binaries are registered in your PATH.

The package manager NPM is also required but is usually bundled with the Node.js installer.

# Setup
This project is development environment agnostic. You can use an IDE or just the command line.
Independently of the environment you will have to `cd` into project root and run `npm install`,
this will download all JavaScript dependencies into `./node_modules`.

Below are recommended setups for common IDEs.

## IntelliJ IDEA
The easiest option is to use the Ultimate Edition of IDEA since it is the only IDE from IntelliJ that
allows having modules of different technologies in one project. Alternatively the free community
editions of IDEA and WebStorm can be used and develop the Java part in IDEA and the frontend in
WebStorm. The following steps show how to setup the project in IDEA Ultimate.

1. Clone this repository from within IDEA
2. When asked, confirm to create a project

# Distribute
To create a distributable package of the software run `npm run distribute` from the project root. It will compile and
bundle all the parts together.

# Documentation
TODO