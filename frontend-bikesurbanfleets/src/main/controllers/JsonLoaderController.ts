import {app} from "electron";
import * as paths from "path";
import * as fs from "fs-extra";
import { Channel, IpcUtil } from "../util";
import { ValidationInfo, validate } from "../../shared/util";
import { JsonFileInfo } from "../../shared/ConfigurationInterfaces";

export interface JsonSchemaGroup {
    globalSchema: any;
    entryPointSchema: any;
    stationsSchema: any;
}

export interface JsonLayoutGroup{
    globalLayout: any;
    //entryPointLayout: any,
    stationsLayout: any;
}

export class JsonLoaderController {

    private globalConfigurationSchema: any = fs.readJsonSync(paths.join(app.getAppPath(), 'schema/global-config.json'));
    private stationConfigurationSchema: any = fs.readJsonSync(paths.join(app.getAppPath(), 'schema/stations-config.json'));
    private entryPointsConfSchema: any = fs.readJsonSync(paths.join(app.getAppPath(), 'schema/entrypoints-config.json'));
    private globalLayout: any = fs.readJsonSync(paths.join(app.getAppPath(), 'schema/global-config-layout.json'));
    //private entryPointLayout: any = fs.readJsonSync(paths.join(app.getAppPath(), 'schema/entrypoints-config-layout.json'));
    private stationsLayout: any = fs.readJsonSync(paths.join(app.getAppPath(), 'schema/stations-config-layout.json'));

    static create() {
        return new JsonLoaderController();
    }

    static enableIpc(): void {
        const jsonLoader = this.create();
        IpcUtil.openChannel('json-loader-init', async () => {

            const channels = [
                new Channel('get-all-schemas', async () => await jsonLoader.getAllSchemas()),
                new Channel('write-json-global', async (jsonInfo: JsonFileInfo) => await jsonLoader.saveGlobalConfig(jsonInfo)),
                new Channel('write-json-entry-points', async (jsonInfo: JsonFileInfo) => await jsonLoader.saveEntryPointsConfig(jsonInfo)),
                new Channel('write-json-stations', async (jsonInfo: JsonFileInfo) => await jsonLoader.saveStationsConfig(jsonInfo)),
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
        return {
            globalSchema: this.globalConfigurationSchema,
            entryPointSchema: this.entryPointsConfSchema,
            stationsSchema: this.stationConfigurationSchema
        };
    }
    async getAllLayouts(): Promise<JsonLayoutGroup> {
        return {
            globalLayout: this.globalLayout,
            //entryPointLayout : this.entryPointLayout,
            stationsLayout: this.stationsLayout
        };
    }

    async loadGlobalConfig(path: string): Promise<any> {
        try {
            return await this.loadAndValidateJson(this.globalConfigurationSchema, path);
        }
        catch (e){ 
            throw new Error(e);
        }
    }

    async loadEntryPointsConfig(path: string): Promise<any> {
        try {
            return await this.loadAndValidateJson(this.entryPointsConfSchema, path);
        }
        catch(e) {
            throw new Error(e);
        }
    }

    async loadStationsConfig(path: string) {
        try {
            return await this.loadAndValidateJson(this.stationConfigurationSchema, path);
        }
        catch(e) {
            throw new Error(e);
        }
    }

    async saveGlobalConfig(jsonInfo: JsonFileInfo) {
        try {
            return await this.saveAndValidateJson(this.globalConfigurationSchema, jsonInfo.path, jsonInfo.data);
        }
        catch(e) {
            throw new Error(e);
        }
    }

    async saveEntryPointsConfig(jsonInfo: JsonFileInfo) {
        try {
            return await this.saveAndValidateJson(this.entryPointsConfSchema, jsonInfo.path, jsonInfo.data);
        }
        catch(e) {
            throw new Error(e);
        }
    }

    async saveStationsConfig(jsonInfo: JsonFileInfo) {
        try {
            return await this.saveAndValidateJson(this.stationConfigurationSchema, jsonInfo.path, jsonInfo.data);
        }
        catch(e) {
            throw new Error(e);
        }
    }

    private async loadAndValidateJson(schema: any, path: string): Promise<any> {
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

    private async writeJson(path:string, data: any): Promise<boolean> {
        try {
            await fs.writeFile(path, JSON.stringify(data, null, 4));
            return true;
        }
        catch (e) {
            throw new Error(e);
        }
    }

    private async saveAndValidateJson(schema: any, path:string, data: any): Promise<any> {
        try {
            let validation: ValidationInfo = validate(schema, data);
            if(!validation.result) {
                throw new Error(validation.errors);
            }
            this.writeJson(path, data);
            return true;
        }
        catch (e) {
            throw new Error(e);
        }
    }
}