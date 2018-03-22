import * as paths from 'path';
import { spawn } from 'child_process'
import { app } from 'electron';
import { IpcUtil } from './index';
import {Main} from "../main";
import {CoreSimulatorArgs, UserGeneratorArgs} from "../../shared/BackendInterfaces";


class Channel {
    constructor(public name: string, public callback: (data?: any) => Promise<any>) {}
}

export default class BackendCalls {

    /*
    *   =================
    *   Channels        |
    *   =================
    */
    private static channels: Channel[] = [];
    private window: Electron.BrowserWindow | null;

    /*
    *   =================
    *   Schemas         |
    *   =================
    */

    private globalSchemaPath = paths.join(app.getAppPath(), 'schema/global-config.json');
    private stationsSchema = paths.join(app.getAppPath(), 'schema/stations-config.json');
    private entryPointSchema = paths.join(app.getAppPath(), 'schema/entrypoints-config.json');
    private usersConfigSchema = paths.join(app.getAppPath(), 'schema/users-config.json');

    /*
    *   =================
    *   json-validator  |
    *   =================
    */

    private jsonSchemaValidator = paths.join(app.getAppPath(), 'jsonschema-validator/jsonschema-validator.js')

    static async create(): Promise<BackendCalls> {
        let backendCalls = new BackendCalls();
        return backendCalls;
    }

    static enableIpc(): void {
        IpcUtil.openChannel('backend-call-init', async () => {
            const backendCalls = await BackendCalls.create();

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

    static stopIpc(): void {
        IpcUtil.closeChannels('backend-call-close', ...this.channels.map((channel) => channel.name));
        this.enableIpc();
    }

    private constructor() {
        this.window = Main.simulate;
    }

    generateUsers(args: UserGeneratorArgs): Promise<void>{
        return new Promise((resolve: any, reject: any) => {

            let rootPath = app.getAppPath();

            const userGen = spawn('java', [
                '-jar',
                `bikesurbanfleets-config-usersgenerator-1.0.jar`,
                `-entryPointsSchema`, this.entryPointSchema,
                `-globalSchema`, this.globalSchemaPath,
                `-entryPointsInput`, args.entryPointsConf,
                `-globalInput`, args.globalConf,
                `-output`, args.outputUsers + "/users-configuration.json",
                `-validator`, this.jsonSchemaValidator
            ], {
                cwd: rootPath,
                shell: false
            });

            userGen.stderr.on('data', (data) => {
                if(this.window) {
                    this.window.webContents.send('user-gen-error', data.toString());
                }
            });

            userGen.stdout.on('data', (data) => {
                if(this.window) {
                    this.window.webContents.send('user-gen-data', data.toString());
                }
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
        });
    }

    simulate(args: CoreSimulatorArgs): Promise<void> {
        return new Promise((resolve: any, reject: any) => {
            let rootPath = app.getAppPath();
            const sim = spawn('java', [
                '-jar',
                'bikesurbanfleets-core-1.0.jar',
                `-globalSchema ${this.globalSchemaPath}`,
                `-usersSchema ${this.usersConfigSchema}`,
                `-stationsSchema ${this.stationsSchema}`,
                `-globalConfig ${args.globalConf}`,
                `-usersConfig ${args.usersConf}`,
                `-stationsConfig ${args.stationsConf}`,
                `-historyOutput ${args.outputHistory}`,
                `-validator ${this.jsonSchemaValidator}`
            ], {
                cwd: rootPath,
                shell: true
            });

            sim.stderr.on('error', (error) => {
                if(this.window) {
                    console.log(error.toString());
                    this.window.webContents.send('core-error', error);
                }
            });

            sim.stdout.on('data', (data) => {
                if (this.window) {
                    console.log(data.toString());
                    this.window.webContents.send('core-data', data);
                }
            });

            sim.on('close', (code) => {
                if (code === 0) {
                    console.log("Simulation Finished");
                    resolve();
                }
                else {
                    reject("Fail executing bikesurbanfleets-core-1.0.jar")
                }
            });
        });
    }



}