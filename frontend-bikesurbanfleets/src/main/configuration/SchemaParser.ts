import {SchemaConfig} from "../../shared/ConfigurationInterfaces";
import * as _ from "lodash";

export default class {

    static async getEntryPointSchemaList(configSch: SchemaConfig): Promise<Array<SchemaConfig>> {
        return configSch.properties.entryPoints.items.anyOf;
    }

    static async readEntryPointTypes(configSch: SchemaConfig): Promise<Array<string>> {
        let entryPointTypes: Array<string> = [];
        for(let entryPointSchema of await this.getEntryPointSchemaList(configSch)) {
            let entryPointProperties = entryPointSchema.properties;
            entryPointTypes.push(entryPointProperties.entryPointType.const);
        }
        return entryPointTypes;
    }

    static async readUserTypes(configSch: SchemaConfig): Promise<Array<string>> {
        let userTypes: Array<string> = [];
        let entryPointSchemas: Array<SchemaConfig> = await this.getEntryPointSchemaList(configSch);
        if(await entryPointSchemas.length !== 0) {
            let entryPointProperties = entryPointSchemas[0].properties;
            for(let userSchema of entryPointProperties.userType.anyOf) {
                let userProperties = userSchema.properties;
                userTypes.push(userProperties.typeName.const);
            }
        }
        return userTypes;
    }

    static async getEntryPointSchema(configSch: SchemaConfig, entryPointType: string, userType: string): Promise <SchemaConfig | undefined> {
        for(let entryPointSchema of await this.getEntryPointSchemaList(configSch)) {
            let entryPointProperties = entryPointSchema.properties;

            //check entry point type
            if(entryPointProperties.entryPointType.const === entryPointType) {
                let userSchemas = entryPointProperties.userType.anyOf;
                for(let userSchema of userSchemas) {
                    let userProperties = userSchema.properties;
                    //Check user type
                    if(userProperties.typeName.const === userType) {
                        let finalEntryPointSchema = _.cloneDeep(entryPointSchema);
                        delete finalEntryPointSchema.properties.userType;

                        finalEntryPointSchema.properties.userType = userSchema;
                        userSchema.additionalProperties = true;
                        if(finalEntryPointSchema.required !== undefined) {
                            _.pull(finalEntryPointSchema.required, 'entryPointType');
                        }
                        finalEntryPointSchema.additionalProperties = true;
            //            console.log(JSON.stringify(finalEntryPointSchema));
                        return finalEntryPointSchema;
                    }
                }
            }
        }
        
    }

    static async getStationSchema(configSch: SchemaConfig): Promise <SchemaConfig | undefined> {
        let stationSchema = configSch.properties.stations.items;
        let bikeSchemas: Array<any> = stationSchema.properties.bikes.anyOf;
        let finalBikeSchema = bikeSchemas[0];
        let finalStationSchema = _.cloneDeep(stationSchema);
        delete finalStationSchema.properties.bikes.anyOf;
        finalStationSchema.properties.bikes = finalBikeSchema;
        delete finalStationSchema.properties.bikes.maximum;
        return finalStationSchema;
    }

    static async getGlobalSchema(configSch: SchemaConfig): Promise<SchemaConfig | undefined> {
        let finalGlobalSchema: any = _.cloneDeep(configSch);
        delete finalGlobalSchema.$schema;
        delete finalGlobalSchema.properties.reservationTime.maximum; //TODO reference from total time
        return finalGlobalSchema;
    }
}