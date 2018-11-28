#!/usr/bin/env node
const fs = require('fs-extra');
const { spawn } = require('child_process');
const path = require('path');
const rootPath = () => process.cwd();
const csv=require('csvtojson');
const json2csv = require('json2csv');
const random = require('random');
const seedrandom = require('seedrandom');
const utils = require('./utils')


rootPath.configurationFiles = () => path.join(rootPath(), 'configuration-files');
rootPath.map = () => path.join(rootPath.projectRoot(), 'backend-configuration-files/maps/madrid.osm');
rootPath.projectRoot = () => path.join(rootPath(), '../../');
rootPath.build = () => path.join(rootPath.projectRoot(), 'build');
rootPath.build.schema = () => path.join(rootPath.build(), 'schema');
rootPath.build.jsonSchemaValidator = () => path.join(rootPath.build(), 'jsonschema-validator');
rootPath.temp = () => path.join(rootPath(), 'temp');
rootPath.temp.hist = () => path.join(rootPath.temp(), 'history');
rootPath.temp.allHist = () => path.join(rootPath.temp(), 'all_histories');
rootPath.temp.csv = () => path.join(rootPath.temp(), 'csv');
rootPath.temp.generatedCsv = () => path.join(rootPath.temp(), 'generated-csv');
rootPath.temp.concurrentBackends = () => path.join(rootPath.temp(), 'concurrent-backend');

let globalConfiguration = 'global-configuration.json';
let stationsConfiguration = 'stations-configuration.json';
let entryPointConfigurationList = [
    'entry-points-configuration-uninformed.json',
    'entry-points-configuration-informed.json',
    'entry-points-configuration-obedient.json',
    'entry-points-configuration-informedR.json',
    'entry-points-configuration-obedientR.json',
    'entry-points-configuration-informed-and-obedient.json',
    //'entry-points-configuration-informedR-and-obedientR.json'
];
let totalTime = 10800;
//let numUsersList = [1250, 1500, 1750, 2000, 2250, 2500, 2750, 3000, 3250, 3500];
let numUsersList = [1000, 2000, 3000, 4000, 5000, 6000, 7000, 8000, 9000, 10000];

function generateDestinationPoint(boundingBox) {
    let latitude = random.float(min = boundingBox.northWest.latitude, max = boundingBox.southEast.latitude);
    let longitude = random.float(min = boundingBox.northWest.longitude, max = boundingBox.southEast.longitude);
    return destinationPoint = {
        latitude: latitude,
        longitude: longitude
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

function writeDestinationPlaces(bbox) {
    usersConfiguration = fs.readJsonSync(`${rootPath.configurationFiles()}/users-configuration.json`);
    for(let i = 0; i < usersConfiguration.initialUsers.length; i++) {
        let destinationPlace = generateDestinationPoint(bbox);
        usersConfiguration.initialUsers[i].destinationPlace = destinationPlace;
    }
    fs.writeFileSync(`${rootPath.configurationFiles()}/users-configuration.json`, JSON.stringify(usersConfiguration, null, 4));
}

function generateUsersConfig(totalTime, numUsers, globalConf, entryPointConf, bbox) {
    return new Promise((resolve, reject) => {
        let entryPointsFile = fs.readJsonSync(path.join(rootPath.configurationFiles(), entryPointConf));
        let numUsersByEntryPoint = numUsers / entryPointsFile.entryPoints.length;
        let lambda = 1/((totalTime/numUsersByEntryPoint) / 60);
        for(let i = 0; i < entryPointsFile.entryPoints.length; i++) {
            entryPointsFile.entryPoints[i].totalUsers = Math.floor(numUsersByEntryPoint);
            entryPointsFile.entryPoints[i].userType.parameters.maxDistanceToRent = 300;
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
                shell: true,
                stdio: 'inherit'
            });

        userGen.on('error', (error) => {
            log.red(error).echo();
        });

        userGen.on('close', (code) => {
            if (code === 0) {
                writeDestinationPlaces(bbox);
                resolve();
            } else {
                log.time().red(`Exit code: ${code}`).echo();
                reject();
            }
        });
    });
}

function simulate() {
    return new Promise((resolve, reject) => {
        let configurationFile = fs.readJsonSync(path.join(rootPath.configurationFiles(), globalConfiguration));
        fs.writeFileSync(path.join(rootPath.configurationFiles(), globalConfiguration),
            JSON.stringify(configurationFile, null, 4));
        const simulator = spawn('java', [
            '-jar',
            'bikesurbanfleets-core-1.0.jar',
            `-globalSchema ${rootPath.build.schema()}/global-config.json`,
            `-usersSchema ${rootPath.build.schema()}/users-config.json`,
            `-stationsSchema ${rootPath.build.schema()}/stations-config.json`,
            `-globalConfig ${rootPath.configurationFiles()}/global-configuration.json`,
            `-usersConfig ${rootPath.configurationFiles()}/users-configuration.json`,
            `-stationsConfig ${rootPath.configurationFiles()}/stations-configuration.json`,
            `-mapPath ${rootPath.map()}`,
            `-historyOutput ${rootPath.temp.hist()}`,
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
            `-h ${rootPath.temp.hist()}`,
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
        csv({delimiter: "auto"}).fromFile(path.join(rootPath.temp.csv(), 'global_values.csv'))
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
    let globalConfJson = fs.readJsonSync(path.join(rootPath.configurationFiles(), globalConfiguration));
    let seed = globalConfJson.randomSeed;
    let bbox = globalConfJson.boundingBox;
    utils.createTempFolders(rootPath);
    let demandSatisfactionCSV = [];
    let hireEffieciencyCSV = [];
    let returnEfficiencyCSV = [];
    for(let epConfiguration of entryPointConfigurationList) {
        let demandSatisfaction = {};
        let hireEfficency = {};
        let returnEfficiency = {};
        let simulations = 0;
        for(let numUsers of numUsersList) {
            random.use(seedrandom(seed.toString()));
            await generateUsersConfig(totalTime, numUsers, globalConfiguration, epConfiguration, bbox);
            console.log(`Users generated - ${epConfiguration} - ${numUsers}`);
            await simulate(epConfiguration, numUsers);
            console.log(`Simulated - ${epConfiguration} - ${numUsers}`);
            console.log("Analysing " + epConfiguration + "-" + numUsers);
            await generateAllData();
            let dataAnalysis = await readDataFromCsv();
            newHistoryFolder = path.join(rootPath.temp.allHist(), `${epConfiguration}_${numUsers}`)
            utils.createFolder(newHistoryFolder);
            utils.copyFolderRecursiveSync(rootPath.temp.hist(), newHistoryFolder); 
            utils.deleteFolderRecursive(rootPath.temp.hist())
            console.log(dataAnalysis)
            demandSatisfaction[numUsers.toString()] = dataAnalysis['Demand satisfaction'];
            hireEfficency[numUsers.toString()] = dataAnalysis['Hire eficiency'];
            returnEfficiency[numUsers.toString()] = dataAnalysis['Return eficiency'];
            simulations++;
            console.log("Analysed " + epConfiguration + "-" + numUsers);
        }
        demandSatisfactionCSV.push(demandSatisfaction);
        hireEffieciencyCSV.push(hireEfficency);
        returnEfficiencyCSV.push(returnEfficiency);
    }
    writeCSVs(demandSatisfactionCSV, hireEffieciencyCSV, returnEfficiencyCSV);
}


main();
