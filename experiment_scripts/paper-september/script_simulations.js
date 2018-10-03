#!/usr/bin/env node
const fs = require('fs-extra');
const { spawn } = require('child_process');
const path = require('path');
const rootPath = () => process.cwd();
const csv = require('csvtojson');
const json2csv = require('json2csv');
const lodash = require('lodash');
const parameters = require('./parameters')


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

let userTypes = parameters.userTypes;
console.log(userTypes);

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

function simulate(globalConfigPath, stationsConfigPath, usersConfigPath, historyPath, mapPath, logPath) {
    return new Promise((resolve, reject) => {
        fs.closeSync(fs.openSync(logPath, 'w'));
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
            shell: true
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

        simulator.stdout.on('data', (data) => {
            console.log(data.toString());
            fs.appendFileSync(logPath, data);
        })
    });
}

function generateAllData(historyPath, csvPath) {
    return new Promise((resolve, reject) => {
        const dataAnalyser = spawn('node', [
            'data-analyser.js',
            'analyse',
            `-h ${historyPath}`,
            `-s ${rootPath.build.schema()}`,
            `-c ${csvPath}`
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

        let tempFolderLog = path.join(rootPath.configurationFiles(), conf + "/logs");
        createTempFolders(tempFolderLog);

        let tempCsvFolderLog = path.join(rootPath.configurationFiles(), conf + `/tempCsv`);
        createTempFolders(tempCsvFolderLog);

        usersConfJson = fs.readJsonSync(usersConf).initialUsers;
        for(userType of userTypes) {

            // Temporary user config
            tempUserConfPath = path.join(tempFolderUsersConf, `users_configuration_${userType}.json`);
            
            //Temporal folder for historics
            let historyTempUserPath = path.join(tempFolderHistory, `history_${userType}`);
            createTempFolders(historyTempUserPath);

            //Temporal folder for logs
            let csvTempUserPath = path.join(tempCsvFolderLog, `csv_${userType}`)
            createTempFolders(csvTempUserPath)

            //Temporal file for logs
            let logFilePath = path.join(tempFolderLog, `log_${userType}.txt`);

            //map file
            let mapPath = path.join(rootPath.mapPath(), 'madrid.osm');
            
            //Generate new users configuration by type of user
            newUsersConf = { initialUsers : []};
            for(user of usersConfJson) {
                let newUser = user;
                if(userType === "USER_INFORMED" || userType === "USER_OBEDIENT") {
                    newUser.userType.parameters = {
                        minReservationAttempts: 0,
                        minReservationAttempts: 0,
                        minRentalAttempts: 3
                    }
                }
                newUser.userType.typeName = userType;
                newUsersConf.initialUsers.push(newUser);
            }
            fs.writeFileSync(tempUserConfPath, JSON.stringify(newUsersConf, null, 4));
            
            //simulate
            await simulate(globalConf, stationsConf, tempUserConfPath, historyTempUserPath, mapPath, logFilePath);
            console.log(csvTempUserPath);
            await generateAllData(historyTempUserPath, csvTempUserPath)
        }
    }
}


main();
