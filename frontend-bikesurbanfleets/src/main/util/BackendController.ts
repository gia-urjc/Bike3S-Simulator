import * as paths from 'path';
import * as Ajv from 'ajv';
import * as fs from 'fs-extra';
import { spawn } from 'child_process';
import { app } from 'electron';
import { IpcUtil } from './index';
import {Main} from "../main";
import {CoreSimulatorArgs, UserGeneratorArgs} from "../../shared/BackendInterfaces";
import Channel from './Channel';

interface ValidationInfo {
    result: boolean;
    errors: string;
}

interface ArgumentInfo {
    title: string;
    argument: string | undefined;
}

class BackendCoreArguments {
    globalConfigurationPath: ArgumentInfo;
    usersConfigurationPath: ArgumentInfo;
    stationsConfigurationPath: ArgumentInfo;
    map: ArgumentInfo;
    historyOuputPath: ArgumentInfo;
}

class BackendUserGenArguments {
    globalConfigurationPath: ArgumentInfo;
    entryPointsPath: ArgumentInfo;
    usersOutputPath: ArgumentInfo;
}


export default class BackendController {

    /*
    *   =================
    *   Channels        |
    *   =================
    */
    private static channels: Channel[] = [];
    private readonly window: Electron.BrowserWindow | null;

    /*
    *   =================
    *   Schemas         |
    *   =================
    */

    private globalSchema = fs.readJsonSync(paths.join(app.getAppPath(), 'schema/global-config.json'));
    private stationsSchema = fs.readJsonSync(paths.join(app.getAppPath(), 'schema/stations-config.json'));
    private entryPointSchema = fs.readJsonSync(paths.join(app.getAppPath(), 'schema/entrypoints-config.json'));
    private usersConfigSchema = fs.readJsonSync(paths.join(app.getAppPath(), 'schema/users-config.json'));

    public static async create(): Promise<BackendController> {
        return new BackendController();
    }

    public static enableIpc(): void {
        IpcUtil.openChannel('backend-call-init', async () => {
            const backendCalls = await BackendController.create();

            this.channels = [
                new Channel('backend-call-generate-users', async (args: UserGeneratorArgs) => await backendCalls.generateUsers(args)),
                new Channel('backend-call-core-simulation', async (args: CoreSimulatorArgs) => await backendCalls.simulate(args))
            ];

            this.channels.forEach((channel) => IpcUtil.openChannel(channel.name, channel.callback));

            IpcUtil.openChannel('backend-call-close', async () => {
                IpcUtil.closeChannels('backend-call-close', ...this.channels.map((channel) => channel.name));
                this.enableIpc();
            });

            IpcUtil.closeChannel('backend-call-init');
        });
    }

    public static stopIpc(): void {
        IpcUtil.closeChannels('backend-call-close', ...this.channels.map((channel) => channel.name));
        this.enableIpc();
    }

    private constructor() {
        this.window = Main.simulate;
    }

    private validateConfiguration(schemaFile: string, configurationFile: string): ValidationInfo {
        let ajv = new Ajv({$data: true});
        console.log(schemaFile);
        console.log(configurationFile);
        let valid = ajv.validate(schemaFile, configurationFile);
        console.log(valid);

        if(valid) {
            return {result: true, errors: ajv.errorsText()};
        }
        return {result: false, errors: ajv.errorsText()};
    }

    private sendInfoToGui(channel: string, message: string): void {
        if(this.window) {
            console.log(message.toString());
            this.window.webContents.send(channel, message);
        }
    }

    public generateUsers(args: UserGeneratorArgs): Promise<void>{
        return new Promise( async (resolve: any, reject: any) => {

            let rootPath = app.getAppPath();
			let entryPointsConf, globalConf: any; 
			try {
				entryPointsConf = await fs.readJson(args.entryPointsConfPath);
				let globalConfData = await fs.readFile(args.globalConfPath);
				let globalConfStr = globalConfData.toString();
				globalConfStr = globalConfStr.replace(/\\/g, "/");
                globalConf = JSON.parse(globalConfStr);
                
                // Entry Point Validation
                let entryPointsValidation = this.validateConfiguration(this.entryPointSchema, entryPointsConf);
                console.log("Validation Entry Points: " + entryPointsValidation);
                if(!entryPointsValidation.result) {
                    this.sendInfoToGui('user-gen-error', entryPointsValidation.errors);
                    reject("Error validating Entry Points: " + entryPointsValidation.errors);
                }
                
                
                //Global Configuration Validation
                let globalValidation = this.validateConfiguration(this.globalSchema, globalConf);
                console.log("Global Validation: " + globalValidation.result);
                if(!globalValidation.result) {
                    this.sendInfoToGui('user-gen-error', globalValidation.errors);
                    reject("Error validating Global Configuration" + globalValidation.errors);
                }

                
                const userGen = spawn('java', [
                    '-jar',
                    'bikesurbanfleets-config-usersgenerator-1.0.jar',
                    '-entryPointsInput', '"' + args.entryPointsConfPath + '"',
                    '-globalInput', '"' + args.globalConfPath + '"',
                    '-output', '"' + args.outputUsersPath + '/users-configuration.json"',
                    '-callFromFrontend'
                ], {
                    cwd: rootPath,
                    shell: true
                });

                userGen.stderr.on('data', (data) => {
                    this.sendInfoToGui('user-gen-error', data.toString());
                });

                userGen.stdout.on('data', (data) => {
                    this.sendInfoToGui('user-gen-data', data.toString());
                });

                userGen.on('close', (code) => {
                    if (code === 0) {
                        console.log('User generation finished');
                        resolve();
                    }
                    else {
                        reject("Fail executing bikesurbanfleets-config-usersgenerator-1.0.jar");
                    }
                });
			}
			catch(error) {
				let errorMessage = "Error reading Configuration Path: \n"
                    + "Global Configuration: " + args.globalConfPath + "\n"
                    + "Entry Points configuration: " + args.entryPointsConfPath + "\n";
                this.sendInfoToGui('user-gen-error', errorMessage);
                reject(errorMessage);
			}
        });
    }

    public simulate(args: CoreSimulatorArgs): Promise<void> {
        return new Promise(async (resolve: any, reject: any) => {
            let rootPath = app.getAppPath();
            let globalConf, stationsConf, usersConf: any;
            try {
				let globalConfData  = await fs.readFile(args.globalConfPath);
				let globalConfStr = globalConfData.toString();
				globalConfStr = globalConfStr.replace(/\\/g, "/");
                globalConf =  JSON.parse(globalConfStr);
                stationsConf = await fs.readJson(args.stationsConfPath);
                usersConf = await fs.readJsonSync(args.usersConfPath);

                //Global Configuration Validation
                let globalValidation = this.validateConfiguration(this.globalSchema , globalConf);
                console.log("Global Validation: " + globalValidation.result);
                if(!globalValidation.result) {
                    this.sendInfoToGui('core-error', globalValidation.errors);
                    reject("Error validating Global Configuration" + globalValidation.errors);
                }

                //Stations Configuration Validation
                let stationsValidation = this.validateConfiguration(this.stationsSchema, stationsConf);
                console.log("Stations Validation " + stationsValidation.result);
                if(!stationsValidation.result) {
                    this.sendInfoToGui('core-error', stationsValidation.errors);
                    reject("Error validating stations" + stationsValidation.errors);
                }

                //User generation validation
                let usersValidation = this.validateConfiguration(this.usersConfigSchema, usersConf);
                console.log("Users Validation " + usersValidation);
                if(!usersValidation.result) {
                    this.sendInfoToGui('core-error', usersValidation.errors);
                    reject("Error validating users", + usersValidation.errors);
                }
            
                    
                const sim = spawn('java', [
                    '-DLogFilePath=${HOME}/.Bike3S/',
                    '-jar',
                    'bikesurbanfleets-core-1.0.jar',
                    '-globalConfig', '"' + args.globalConfPath + '"',
                    '-usersConfig', '"' + args.usersConfPath + '"',
                    '-stationsConfig', '"' + args.stationsConfPath + '"',
                    '-historyOutput', '"' + args.outputHistoryPath + '"',
                    '-mapPath', '"' + args.mapPath + '"',
                    `-callFromFrontend`
                ], {
                    cwd: rootPath,
                    shell: true
                });


                sim.stderr.on('data', (error) => {
                    this.sendInfoToGui('core-error', error.toString());
                });

                sim.stdout.on('data', (data) => {
                    this.sendInfoToGui('core-data', data.toString());
                });

                sim.on('close', (code) => {
                    if (code === 0) {
                        console.log("Simulation Finished");
                        resolve();
                    }
                    else {
                        reject("Fail executing bikesurbanfleets-core-1.0.jar");
                    }
                });
            }
            catch(error) {
                let errorMessage = "Error reading Configuration Path: \n"
                    + "Global Configuration: " + args.globalConfPath + "\n"
                    + "Users Configuration: " + args.usersConfPath + "\n"
                    + "Stations Configuration: " + args.stationsConfPath + "\n"
                    + "Map Path: " + args.mapPath + "\n";
                    
                this.sendInfoToGui('core-error', errorMessage);
                reject(errorMessage);
            }
        });
        
    }

}