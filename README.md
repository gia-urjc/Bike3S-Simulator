# Prerequisites
1. JDK 1.8 (Java 8 is required, other versions won't work)
2. Maven (tested with 3.5)
3. Node.js (tested with versions 8 and 9)

Please make sure that all the binaries are registered in your PATH.

The package manager NPM is also required but is usually bundled with the Node.js installer.

# Setup
`cd` into project root and run `npm install`, this will download all JavaScript dependencies into `./node_modules`.

You can use the development environment of your choice, if you use an IDE it is recommended to setup the Java backend
root `./backend-bikesurbanfloats` as a separate Maven module.

# Distribute
To create a distributable package of the software run `npm run distribute` from the project root. It will compile and
bundle all the parts together.

# Documentation
TODO