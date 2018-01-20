import * as fs from 'fs-extra';
import { app } from 'electron';
import * as paths from 'path';
import {SchemaConfig} from "../../shared/configuration";
import {IpcUtil} from "../util";

class Channel {
    constructor(public name: string, public callback: (data?: any) => Promise<any>) {}
}

export default class SchemaFormGenerator{

    private static configurationFile: any = fs.readJsonSync(paths.join(app.getAppPath(), 'schema/initial-config.json'));

    static create() {
        return new SchemaFormGenerator();
    }

    static enableIpc(): void {
        let schemaFormGenerator = SchemaFormGenerator.create();
        IpcUtil.openChannel('form-schema-init', async () => {

            const channels = [
                new Channel('form-schema-entry-user-type', async () => await schemaFormGenerator.schemaFormEntryPointAndUserTypes())
            ];

            channels.forEach((channel) => IpcUtil.openChannel(channel.name, channel.callback));

            IpcUtil.openChannel('form-schema-close', async () => {
                IpcUtil.closeChannels('form-schema-close', ...channels.map((channel) => channel.name));
                this.enableIpc();
            });

            IpcUtil.closeChannel('form-schema-init');
        });
    }

    private constructor() {}

    private getEntryPointSchemas(): Array<SchemaConfig> {
        return SchemaFormGenerator.configurationFile.properties.entryPoints.items.anyOf;
    }

    private getUserTypeSchemas(): Array<SchemaConfig> {
        let entryPointSchemas: Array<any> = this.getEntryPointSchemas();
        if(entryPointSchemas.length === 0) {
            return [];
        }
        return entryPointSchemas[0].properties.userType.anyOf;
    }

    private readEntriesAndUserTypes(entryPointTypes: Array<string>, userTypes: Array<string>) {
        let usersRead = false;
        for (let entryPointSchema of this.getEntryPointSchemas()) {
            let entryPointProperties = entryPointSchema.properties;
            entryPointTypes.push(entryPointProperties.entryPointType.const);
            if (!usersRead) {
                for (let userSchema of this.getUserTypeSchemas()) {
                    let userProperties = userSchema.properties;
                    userTypes.push(userProperties.typeName.const);
                }
                usersRead = true;
            }
        }
    }

    public schemaFormEntryPointAndUserTypes(): SchemaConfig {
        let schema: SchemaConfig;
        let entryPointTypes: Array<string> = [];
        let userTypes: Array<string> = [];
        this.readEntriesAndUserTypes(entryPointTypes, userTypes);
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
}


