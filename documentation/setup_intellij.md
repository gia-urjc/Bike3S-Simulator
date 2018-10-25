# Backend Setup - IntelliJ IDEA CE
## Instructions

First of all be sure to have every prerequisite installed. Prerequisites are defined in 

### 1. Import the project
 - Open IntelliJ IDEA. 
 - Select Import Project.
 - The folder you should select is `/backend-bikesurbanfleets/`
 - Check this checkbox: `Search for projects recursively`, `Import Maven projects automatically` and `Create module groups for multi-module Maven Projects`
 
 ![Gif with instructions to import the project](gifs/backend_intellij_1.gif)

### 2. Run the project with IntelliJ
As you can see this project is separated in modules. 

![Modules image](images/modules_intellij.png)

The module `bikesurbanfleets-config-usergenerator` generates users, and the module `bikesurbanfleets-core` simulates. 
We should create two Run Configurations, one to generate users, and the other, to simulate, in order to run our code with the IDE.

#### Users generator configuration 
- Click on `Run` &rarr; `Edit Configuration`.
- Select the button `+` &rarr; `Application` and name this Run Configuration as you want.
- Select in Main class the `Application.java` of the `backend-bikesurbanfleets-config-usersgenerator`
- Inside `Program arguments`, copy and paste the next arguments: 

```
-entryPointsSchema ../build/schema/entrypoints-config.json
-globalSchema ../build/schema/global-config.json 
-entryPointsInput ../backend-configuration-files/example-configuration/entry-points-configuration.json
-globalInput ../backend-configuration-files/example-configuration/global-configuration.json 
-output ../backend-configuration-files/example-configuration/users-configuration.json
-validator ../build/jsonschema-validator/jsonschema-validator.js
```

- Select on `Use classpath of module` the module: `bikesurbanfleets-config-usersgenerator`.
- Click `Apply`, then click `Ok`.  

![Gif with instructions to import the project](gifs/backend_intellij_2.gif)

#### Core configuration 
- Click on `Run` &rarr; `Edit Configuration`.
- Select the button `+` &rarr; `Application`.
- Select in Main class the `Application.java` of the `backend-bikesurbanfleets-core` and name this Run Configuration as you want.
- Inside `Program arguments`, copy and paste the next arguments:

```
-globalSchema ../build/schema/global-config.json
-usersSchema ../build/schema/users-config.json
-stationsSchema ../build/schema/stations-config.json
-globalConfig ../backend-configuration-files/example-configuration/global-configuration.json
-usersConfig ../backend-configuration-files/example-configuration/users-configuration.json
-stationsConfig ../backend-configuration-files/example-configuration/stations-configuration.json
-mapPath ../backend-configuration-files/maps/madrid.osm
-validator ../build/jsonschema-validator/jsonschema-validator.js
```

- Select on `Use classpath of module` the module: `bikesurbanfleets-core`.
- Click `Apply`, then click `Ok`.  

![Gif with instructions to import the project](gifs/backend_intellij_3.gif)

# Common problems

> I can't run the simulator

Be sure of accomplish the Prerequisites and Setup section of the [Quick Start Guide](../README.md)
