import {SchemaConfig} from "../../shared/ConfigurationInterfaces";
import * as _ from "lodash";

export default class {

    static getEntryPointSchemaList(configSch: SchemaConfig): Array<SchemaConfig> {
        return configSch.properties.entryPoints.items.anyOf;
    }

    static getRecommendersSchemaList(configSch: SchemaConfig): Array<SchemaConfig> {
        return configSch.properties.recommendationSystemType.anyOf;
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
        let entryPointSchemas: Array<SchemaConfig> = this.getEntryPointSchemaList(configSch);
        if(await entryPointSchemas.length !== 0) {
            let entryPointProperties = entryPointSchemas[0].properties;
            for(let userSchema of entryPointProperties.userType.anyOf) {
                let userProperties = userSchema.properties;
                userTypes.push(userProperties.typeName.const);
            }
        }
        else {
            throw new Error("No user types specified in schemas");
        }
        return userTypes;
    }

    static async readRecommendersTypes(configSch: SchemaConfig): Promise<Array<string>> {
        let recommendersTypes: Array<string> = [];
        let recommendersSchemas: Array<SchemaConfig> = this.getRecommendersSchemaList(configSch);
        if(recommendersSchemas.length !== 0) {
            for(let recommendersSchema of recommendersSchemas) {
                let recommenderProperties = recommendersSchema.properties;
                recommendersTypes.push(recommenderProperties.typeName.const);
            }
        }
        else {
            throw new Error("No recommenders specified in schemas");
        }
        return recommendersTypes;
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
                        // console.log(JSON.stringify(finalEntryPointSchema));
                        return finalEntryPointSchema;
                    }
                }
            }
        }
        
    }

    static async getRecommenderSchema(configSch: SchemaConfig, recommenderType: string): Promise<SchemaConfig | undefined> {
        for(let recommenderSchema of await this.getRecommendersSchemaList(configSch)) {
            if(recommenderSchema.properties.typeName.const === recommenderType) {
                delete recommenderSchema.properties.typeName;
                return recommenderSchema; 
            }
        }
    }

    static async getStationSchema(configSch: SchemaConfig): Promise <SchemaConfig | undefined> {
        let stationSchema = configSch.properties.stations.items;
        let bikeSchemas: Array<any> = stationSchema.properties.bikes.anyOf;
        let finalBikeSchema = bikeSchemas[0];
        let finalStationSchema = _.cloneDeep(stationSchema);
        let schProperties = finalStationSchema.properties;
        delete schProperties.bikes.anyOf;
        schProperties.bikes = finalBikeSchema;
        delete schProperties.bikes.maximum;
        delete schProperties.availablebikes;
        delete schProperties.reservedbikes;
        delete schProperties.reservedslots;
        delete schProperties.availableslots;
        console.log(finalStationSchema);
        return finalStationSchema;
    }

    static async getGlobalSchema(configSch: SchemaConfig): Promise<SchemaConfig | undefined> {
        let finalGlobalSchema: any = _.cloneDeep(configSch);
        delete finalGlobalSchema.$schema;
        delete finalGlobalSchema.properties.reservationTime.maximum; //TODO reference from total time
        return finalGlobalSchema;
    }
}