# Prerequisites
1. JDK 1.8
2. Maven 3.5
3. Node.js 8.9

Please make sure that all the binaries are registered in your PATH.

The package manager NPM is also required but is usually bundled with the Node.js installer.

# Setup
This project is development environment agnostic. You can use an IDE or just the command line.

Below are recommended setups for common IDEs.

## IntelliJ IDEA
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

# Distribute
To create a distributable package of the software run `npm run distribute` from the project root. It will compile and
bundle all the parts together.

# Documentation
TODO