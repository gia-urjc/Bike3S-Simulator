import {app} from "electron";
import * as paths from "path";
import * as fs from "fs-extra";
import {IpcUtil} from "../util";

class Channel {
    constructor(public name: string, public callback: (data?: any) => Promise<any>) {}
}

export interface JsonSchemaGroup {
    globalSchema: any;
    entryPointSchema: any;
    stationsSchema: any;
}

export interface JsonInfo {
    json: any;
    path: string;
}

export default class JsonLoader {

    private static globalConfigurationSchema: any = fs.readJsonSync(paths.join(app.getAppPath(), 'schema/global-config.json'));
    private static stationConfigurationSchema: any = fs.readJsonSync(paths.join(app.getAppPath(), 'schema/stations-config.json'));
    private static entryPointsConfSchema: any = fs.readJsonSync(paths.join(app.getAppPath(), 'schema/entrypoints-config.json'));

    static create() {
        return new JsonLoader();
    }

    static enableIpc(): void {
        IpcUtil.openChannel('json-loader-init', async () => {

            const channels = [
                new Channel('get-all-schemas', async () => this.getAllSchemas()),
                new Channel('write-json', async (jsonInfo: JsonInfo) => this.writeJson(jsonInfo)),
                new Channel('load-json', async (path: string) => this.loadJson(path))
            ];

            channels.forEach((channel) => IpcUtil.openChannel(channel.name, channel.callback));

            IpcUtil.openChannel('json-loader-close', async () => {
                IpcUtil.closeChannels('json-loader-close', ...channels.map((channel) => channel.name));
                this.enableIpc();
            });

            IpcUtil.closeChannel('json-loader-init');
        });
    }

    static async getAllSchemas(): Promise<JsonSchemaGroup> {
        return {
            globalSchema: this.globalConfigurationSchema,
            entryPointSchema: this.entryPointsConfSchema,
            stationsSchema: this.stationConfigurationSchema
        };
    }

    static async writeJson(jsonInfo: JsonInfo): Promise<boolean> {
        try {
            await fs.writeFile(jsonInfo.path, JSON.stringify(jsonInfo.json, null, 4));
            return true;
        }
        catch (e) {
            throw new Error(e);
        }
    }

    static async loadJson(path: string): Promise<any> {
        try {
            let data = await fs.readJSON(path);
            return data;
        }
        catch (e) {
            throw new Error(e);
        }
    }

    static validateJson(path: s)
}