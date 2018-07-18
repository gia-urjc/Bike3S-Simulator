import * as paths from 'path';
import * as fs from 'fs-extra';
import * as kill from 'tree-kill';
import { spawn, ChildProcess } from 'child_process';
import { app } from 'electron';
import { IpcUtil, Channel } from '../util';
import {Main} from "../main";
import {CoreSimulatorArgs, UserGeneratorArgs} from "../../shared/BackendInterfaces";
import { validate, ValidationInfo } from '../../shared/util';

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

    private userGenProcess?: ChildProcess;
    private simulationProcess?: ChildProcess;

    public static async create(): Promise<BackendController> {
        return new BackendController();
    }

    public static enableIpc(): void {
        IpcUtil.openChannel('backend-call-init', async () => {
            const backendCalls = await BackendController.create();

            this.channels = [
                new Channel('backend-call-generate-users', async (args: UserGeneratorArgs) => await backendCalls.generateUsers(args)),
                new Channel('backend-call-core-simulation', async (args: CoreSimulatorArgs) => await backendCalls.simulate(args)),
                new Channel('backend-call-cancel-simulation', async () => await backendCalls.cancelSimulation())
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

    private sendInfoToGui(channel: string, message: string): void {
        if(this.window) {
            this.window.webContents.send(channel, message);
        }
    }

    public generateUsers(args: UserGeneratorArgs): Promise<void>{
        return new Promise( async (resolve: any, reject: any) => {

            let rootPath = app.getAppPath();
			let entryPointsConf, globalConf: any; 
			try {
                entryPointsConf = await fs.readJson(args.entryPointsConfPath);
                globalConf = await fs.readJson(args.globalConfPath);

                // Entry Point Validation
                let entryPointsValidation: ValidationInfo = validate(this.entryPointSchema, entryPointsConf);
                console.log("Validation Entry Points: " + entryPointsValidation);
                if(!entryPointsValidation.result) {
                    this.sendInfoToGui('user-gen-error', entryPointsValidation.errors);
                    reject("Error validating Entry Points: " + entryPointsValidation.errors);
                }
                
                
                //Global Configuration Validation
                let globalValidation: ValidationInfo = validate(this.globalSchema, globalConf);
                console.log("Global Validation: " + globalValidation.result);
                if(!globalValidation.result) {
                    this.sendInfoToGui('user-gen-error', globalValidation.errors);
                    reject("Error validating Global Configuration" + globalValidation.errors);
                }

                
                this.userGenProcess = spawn('java', [
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

                this.userGenProcess.stderr.on('data', (data) => {
                    this.sendInfoToGui('user-gen-error', data.toString());
                });

                this.userGenProcess.stdout.on('data', (data) => {
                    this.sendInfoToGui('user-gen-data', data.toString());
                });

                this.userGenProcess.on('close', (code) => {
                    this.userGenProcess = undefined;
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
                this.sendInfoToGui('user-gen-error', 'Exception:' + error);
                reject(errorMessage);
			}
        });
    }

    public simulate(args: CoreSimulatorArgs): Promise<void> {
        return new Promise(async (resolve: any, reject: any) => {
            let rootPath = app.getAppPath();
            let globalConf, stationsConf, usersConf: any;
            try {
				globalConf  = await fs.readJson(args.globalConfPath);
                stationsConf = await fs.readJson(args.stationsConfPath);
                usersConf = await fs.readJsonSync(args.usersConfPath);

                //Global Configuration Validation
                let globalValidation: ValidationInfo = validate(this.globalSchema , globalConf);
                console.log("Global Validation: " + globalValidation.result);
                if(!globalValidation.result) {
                    this.sendInfoToGui('core-error', globalValidation.errors);
                    reject("Error validating Global Configuration" + globalValidation.errors);
                }

                //Stations Configuration Validation
                let stationsValidation: ValidationInfo = validate(this.stationsSchema, stationsConf);
                console.log("Stations Validation " + stationsValidation.result);
                if(!stationsValidation.result) {
                    this.sendInfoToGui('core-error', stationsValidation.errors);
                    reject("Error validating stations" + stationsValidation.errors);
                }

                //User generation validation
                let usersValidation: ValidationInfo = validate(this.usersConfigSchema, usersConf);
                console.log("Users Validation " + usersValidation.result);
                if(!usersValidation.result) {
                    this.sendInfoToGui('core-error', usersValidation.errors);
                    reject("Error validating users", + usersValidation.errors);
                }
            
                    
                this.simulationProcess = spawn('java', [
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


                this.simulationProcess.stderr.on('data', (data) => {
                    console.log(data.toString());
                    this.sendInfoToGui('core-error', data.toString());
                });

                this.simulationProcess.stdout.on('data', (data) => {
                    console.log(data.toString());
                    this.sendInfoToGui('core-data', data.toString());
                });

                this.simulationProcess.on('close', (code) => {
                    this.simulationProcess = undefined;
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

    public cancelSimulation(): Promise<void> {
        return new Promise((resolve, reject) => {
            if(this.simulationProcess) {
                kill(this.simulationProcess.pid);
                console.log("Simulation interrupted");
                resolve();
            }
            reject("Proccess is not executing");
        });
    }
    
}