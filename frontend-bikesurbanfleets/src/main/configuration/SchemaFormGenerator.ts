import * as fs from 'fs-extra';
import { app } from 'electron';
import * as paths from 'path';
import {EntryPointDataType, SchemaConfig} from "../../shared/configuration";
import {IpcUtil} from "../util";
import SchemaParser from "./SchemaParser";

class Channel {
    constructor(public name: string, public callback: (data?: any) => Promise<any>) {}
}

export default class SchemaFormGenerator{

    private static configurationSchema: any = fs.readJsonSync(paths.join(app.getAppPath(), 'schema/initial-config.json'));

    static create() {
        return new SchemaFormGenerator();
    }

    static enableIpc(): void {
        IpcUtil.openChannel('form-schema-init', async () => {

            const channels = [
                new Channel('form-schema-entry-user-type', async () => this.schemaFormEntryPointAndUserTypes()),
                new Channel('form-schema-entry-point-by-type', async (dataTypes: EntryPointDataType) => this.schemaFormEntryPointByTypes(dataTypes)),
                new Channel('form-schema-station', async () => this.schemaFormStation())
            ];

            channels.forEach((channel) => IpcUtil.openChannel(channel.name, channel.callback));

            IpcUtil.openChannel('form-schema-close', async () => {
                IpcUtil.closeChannels('form-schema-close', ...channels.map((channel) => channel.name));
                this.enableIpc();
            });

            IpcUtil.closeChannel('form-schema-init');
        });
    }

    public static async schemaFormEntryPointAndUserTypes(): Promise<SchemaConfig> {
        let schema: SchemaConfig;
        let entryPointTypes: Array<string> = await SchemaParser.readEntryPointTypes(this.configurationSchema);
        let userTypes: Array<string> = await SchemaParser.readUserTypes(this.configurationSchema);
        schema = {
            type: "object",
            properties: {
                entryPointType: {
                    type: "string",
                    enum: entryPointTypes
                },
                userType: {
                    type: "string",
                    enum: userTypes
                }
            }
        };
        console.log(schema);
        return schema;
    }

    public static async schemaFormEntryPointByTypes(dataTypes: EntryPointDataType): Promise<SchemaConfig | undefined> {
        let entryPointSchema = await SchemaParser.getEntryPointSchema(this.configurationSchema, dataTypes.entryPointType, dataTypes.userType);
        if(entryPointSchema !== undefined) {
            return entryPointSchema;
        }
        else {
            throw new Error("Entry Point type or user Type is not valid");
        }
    }

    public static async schemaFormStation(): Promise<SchemaConfig | undefined> {
        let stationSchema = await SchemaParser.getStationSchema(this.configurationSchema);
        if(stationSchema !== undefined) {
            return stationSchema
        }
        else {
            throw new Error("Station is not valid or is not defined in schemas");
        }
    }
}


