import {app} from "electron";
import * as paths from "path";
import * as fs from "fs-extra";
import { Channel, IpcUtil } from "../util";
import { ValidationInfo, validate } from "../../shared/util";

export interface JsonSchemaGroup {
    globalSchema: any;
    entryPointSchema: any;
    stationsSchema: any;
}

export interface JsonInfo {
    json: any;
    path: string;
}

export interface JsonLayoutGroup{
    globalLayout: any,
    entryPointLayout: any,
    stationsLayout: any
}

export default class JsonLoader {

    private globalConfigurationSchema: any = fs.readJsonSync(paths.join(app.getAppPath(), 'schema/global-config.json'));
    private stationConfigurationSchema: any = fs.readJsonSync(paths.join(app.getAppPath(), 'schema/stations-config.json'));
    private entryPointsConfSchema: any = fs.readJsonSync(paths.join(app.getAppPath(), 'schema/entrypoints-config.json'));
    private globalLayout: any = fs.readJsonSync(paths.join(app.getAppPath(), 'schema/global-config-layout.json'));
    private entryPointLayout: any = fs.readJsonSync(paths.join(app.getAppPath(), 'schema/entrypoints-config-layout.json'));
    private stationsLayout: any = fs.readJsonSync(paths.join(app.getAppPath(), 'schema/stations-config-layout.json'));

    static create() {
        return new JsonLoader();
    }

    static enableIpc(): void {
        const jsonLoader = this.create();
        IpcUtil.openChannel('json-loader-init', async () => {

            const channels = [
                new Channel('get-all-schemas', async () => await jsonLoader.getAllSchemas()),
                new Channel('write-json', async (jsonInfo: JsonInfo) => await jsonLoader.writeJson(jsonInfo)),
                new Channel('load-json-global', async (path: string) => await jsonLoader.loadGlobalConfig(path)),
                new Channel('load-json-entry-points', async (path: string) => await jsonLoader.loadEntryPointsConfig(path)),
                new Channel('load-json-stations', async (path: string) => await jsonLoader.loadStationsConfig(path)),
                new Channel('get-json-layout', async() => await jsonLoader.getAllLayouts())
            ];

            channels.forEach((channel) => IpcUtil.openChannel(channel.name, channel.callback));

            IpcUtil.openChannel('json-loader-close', async () => {
                IpcUtil.closeChannels('json-loader-close', ...channels.map((channel) => channel.name));
                this.enableIpc();
            });

            IpcUtil.closeChannel('json-loader-init');
        });
    }

    async getAllSchemas(): Promise<JsonSchemaGroup> {
        console.log(JSON.stringify(this.entryPointsConfSchema));
        return {
            globalSchema: this.globalConfigurationSchema,
            entryPointSchema: this.entryPointsConfSchema,
            stationsSchema: this.stationConfigurationSchema
        };
    }
    async getAllLayouts(): Promise<JsonLayoutGroup> {
        console.log(JSON.stringify(this.entryPointLayout))
        return {
            globalLayout: this.globalLayout,
            entryPointLayout : this.entryPointLayout,
            stationsLayout: this.stationsLayout
        }
    }

    async writeJson(jsonInfo: JsonInfo): Promise<boolean> {
        try {
            await fs.writeFile(jsonInfo.path, JSON.stringify(jsonInfo.json, null, 4));
            return true;
        }
        catch (e) {
            throw new Error(e);
        }
    }

    async loadGlobalConfig(path: string): Promise<any> {
        let data = await this.loadAndValidateJson(this.globalConfigurationSchema, path);
        return data;
    }

    async loadEntryPointsConfig(path: string): Promise<any> {
        let data = await this.loadAndValidateJson(this.entryPointsConfSchema, path);
        return data;
    }

    async loadStationsConfig(path: string) {
        let data = await this.loadAndValidateJson(this.stationConfigurationSchema, path);
        return data;
    }

    async loadAndValidateJson(schema:string, path: string): Promise<any> {
        try {
            let data = await fs.readJSON(path);
            let validation: ValidationInfo = validate(schema, data);
            if(!validation.result) {
                throw new Error(validation.errors);
            }
            return data;
        }
        catch (e) {
            throw new Error(e);
        }
    }
}