#!/usr/bin/env node
const fs = require('fs-extra');
const { spawn } = require('child_process');
const path = require('path');
const rootPath = () => process.cwd();
const csv = require('csvtojson');
const json2csv = require('json2csv');
const lodash = require('lodash');


rootPath.configurationFiles = () => path.join(rootPath(), 'configuration-files');
rootPath.projectRoot = () => path.join(rootPath(), '../../');
rootPath.build = () => path.join(rootPath.projectRoot(), 'build');
rootPath.mapPath = () => path.join(rootPath.projectRoot(), 'backend-configuration-files/maps');
rootPath.build.schema = () => path.join(rootPath.build(), 'schema');
rootPath.build.jsonSchemaValidator = () => path.join(rootPath.build(), 'jsonschema-validator');
rootPath.temp = () => path.join(rootPath(), 'temp');
rootPath.temp.history = () => path.join(rootPath.temp(), 'history');
rootPath.temp.csv = () => path.join(rootPath.temp(), 'csv');
rootPath.temp.generatedCsv = () => path.join(rootPath.temp(), 'generated-csv');
rootPath.temp.concurrentBackends = () => path.join(rootPath.temp(), 'concurrent-backend');

let userTypes = ["USER_RANDOM", "USER_INFORMED", "USER_INFORMED", "USER_OBEDIENT", "USER_DISTANCE_RESTRICTION", "USER_REASONABLE", "USER_COMMUTER", "USER_AVAILABLE_RESOURCES", "USER_TOURIST"];

function createTempFolders(folderPath) {
    if(!fs.existsSync(folderPath)) {
        fs.mkdirSync(folderPath);
    }
}
function beforeScript() {
    return new Promise((resolve, reject) => {
        const buildDev = spawn('node', ['fuse', 'build:dev-backend'], {
            cwd: rootPath.projectRoot(),
            shell: true, // necessary for windows
            stdio: 'inherit' // pipe to calling process
        });

        buildDev.on('error', (error) => {
            log.red(error).echo();
        });

        buildDev.on('close', (code) => {
            if (code === 0) {
                resolve();
            } else {
                log.time().red(`Exit code: ${code}`).echo();
                reject();
            }
        });
    });
}

/* 
function generateUsersConfig(totalTime, numUsers, globalConf, entryPointConf) {
    return new Promise((resolve, reject) => {
        let entryPointsFile = fs.readJsonSync(path.join(rootPath.configurationFiles(), entryPointConf));
        let lambda = 1/(totalTime/numUsers);
        for(let i = 0; i < entryPointsFile.entryPoints.length; i++) {
            entryPointsFile.entryPoints[i].totalUsers = numUsers;
            entryPointsFile.entryPoints[i].distribution.lambda = lambda;
        }
        fs.writeFileSync(path.join(rootPath.configurationFiles(), entryPointConf), JSON.stringify(entryPointsFile, null, 4));
        const userGen = spawn('java', [
                '-jar',
                `bikesurbanfleets-config-usersgenerator-1.0.jar`,
                `-entryPointsSchema ${rootPath.build.schema()}/entrypoints-config.json`,
                `-globalSchema ${rootPath.build.schema()}/global-config.json`,
                `-entryPointsInput ${rootPath.configurationFiles()}/${entryPointConf}`,
                `-globalInput ${rootPath.configurationFiles()}/${globalConf}`,
                `-output ${rootPath.configurationFiles()}/users-configuration.json`,
                `-validator ${rootPath.build.jsonSchemaValidator()}/jsonschema-validator.js`
            ], {
                cwd: rootPath.build(),
                shell: true
            });

        userGen.on('error', (error) => {
            log.red(error).echo();
        });

        userGen.on('close', (code) => {
            if (code === 0) {
                resolve();
            } else {
                log.time().red(`Exit code: ${code}`).echo();
                reject();
            }
        });
    });
}
*/

function simulate(globalConfigPath, stationsConfigPath, usersConfigPath, historyPath, mapPath) {
    return new Promise((resolve, reject) => {
        const simulator = spawn('java', [
            '-jar',
            'bikesurbanfleets-core-1.0.jar',
            `-globalSchema ${rootPath.build.schema()}/global-config.json`,
            `-usersSchema ${rootPath.build.schema()}/users-config.json`,
            `-stationsSchema ${rootPath.build.schema()}/stations-config.json`,
            `-globalConfig ${globalConfigPath}`,
            `-usersConfig ${usersConfigPath}`,
            `-stationsConfig ${stationsConfigPath}`,
            `-mapPath ${mapPath}`,
            `-historyOutput ${historyPath}`,
            `-validator ${rootPath.build.jsonSchemaValidator()}/jsonschema-validator.js`
        ], {
            cwd: rootPath.build(),
            shell: true,
            stdio: 'inherit'
        });

        simulator.on('error', (error) => {
            console.error(error);
        });

        simulator.on('close', (code) => {
            if (code === 0) {
                resolve();
            } else {
                console.error(`Exit code: ${code}`);
                reject();
            }
        });
    });
}

function generateAllData() {
    return new Promise((resolve, reject) => {
        const dataAnalyser = spawn('node', [
            'data-analyser.js',
            'analyse',
            `-h ${rootPath.temp.history()}`,
            `-s ${rootPath.build.schema()}`,
            `-c ${rootPath.temp.csv()}`
        ], {
            cwd: path.join(rootPath.build(), 'data-analyser'),
            shell: true,
            stdio: 'inherit'
        })

        dataAnalyser.on('error', (error) => {
            console.error(error);
        });

        dataAnalyser.on('close', (code) => {
            if (code === 0) {
                resolve();
            } else {
                console.error(`Exit code: ${code}`);
                reject();
            }
        });
    });
}

function readDataFromCsv() {
    return new Promise((resolve, reject) => {
        csv().fromFile(path.join(rootPath.temp.csv(), 'global_values.csv'))
            .on('json',(jsonObj)=>{
                resolve(jsonObj);
            })
            .on('done',(error)=>{
                reject(error);
                console.log('end')
            })
    });
}


function writeCSVs(dataDS, dataHE, dataRE) {
    let fields = [];
    for(numUsers of numUsersList) {
        fields.push(numUsers.toString());
    }
    let DScsv = json2csv({ data: dataDS, fields: fields });
    let HEcsv = json2csv({ data: dataHE, fields: fields });
    let REcsv = json2csv({ data: dataRE, fields: fields });

    let csvName = "DS.csv";
    fs.writeFileSync(path.join(rootPath.temp.generatedCsv(), csvName), DScsv);

    csvName = "HE.csv";
    fs.writeFileSync(path.join(rootPath.temp.generatedCsv(), csvName), HEcsv);

    csvName = "RE.csv";
    fs.writeFileSync(path.join(rootPath.temp.generatedCsv(), csvName), REcsv);

}

async function main() {
    //await beforeScript();
    let configurations = lodash.without(fs.readdirSync(rootPath.configurationFiles()), '.DS_Store');
    for(conf of configurations) {
        let globalConf = path.join(rootPath.configurationFiles(), conf + "/global-configuration.json");
        let stationsConf = path.join(rootPath.configurationFiles(), conf + "/stations_configuration.json");
        let usersConf = path.join(rootPath.configurationFiles(), conf + "/users_configuration.json");

        let tempFolderUsersConf = path.join(rootPath.configurationFiles(), conf + "/tempUsersConf");
        createTempFolders(tempFolderUsersConf);
        
        let tempFolderHistory = path.join(rootPath.configurationFiles(), conf + "/tempHistory");
        createTempFolders(tempFolderHistory);

        usersConfJson = fs.readJsonSync(usersConf).initialUsers;
        for(userType of userTypes) {

            // Temporary user config
            tempUserConfPath = path.join(tempFolderUsersConf, `users_configuration_${userType}.json`);
            
            //Temporal folder for historics
            let historyTempUserPath = path.join(tempFolderHistory, `history_${userType}`);
            createTempFolders(historyTempUserPath);

            //map file
            let mapPath = path.join(rootPath.mapPath(), 'madrid.osm');
            
            //Generate new users configuration by type of user
            newUsersConf = { initialUsers : []};
            for(user of usersConfJson) {
                let newUser = user;
                newUser.userType.typeName = userType;
                newUsersConf.initialUsers.push(newUser);
            }
            fs.writeFileSync(tempUserConfPath, JSON.stringify(newUsersConf, null, 4));
            
            //simulate
            await simulate(globalConf, stationsConf, tempUserConfPath, historyTempUserPath, mapPath);
        }
    }
}


main();
