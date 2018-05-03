# Backend Setup - IntelliJ IDEA
## Instructions

1. ### Import the project
 - Open IntelliJ IDEA. 
 - Select Import Project.
 - The folder you should select is `/backend-bikesurbanfleets/`
 - Check this checkbox: `Search for projects recursively`, `Import Maven projects automatically` and `Create module groups for multi-module Maven Projects`
 
 ![Gif with instructions to import the project](gifs/backend_intellij_1.gif "Instructions 1")

2. As you can see this project is separated in modules. 

![Modules image](images/modules_intellij.png "Instructions 2")

The module `bikesurbanfleets-config-usergenerator` generates users, and the module `bikesurbanfleets-core` simulates. 
We should create two Run Configurations. One to generate users, and the other to simulate, to run our code with the IDE.

- Click on `Run` &rarr; `Edit Configuration`.
- Select `Default` &rarr; `Application`.
- 


