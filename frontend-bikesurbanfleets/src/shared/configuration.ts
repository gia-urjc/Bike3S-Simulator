import { Geo, PlainObject } from './util';

export type SchemaConfig = {
    type: string;
    properties?: any;
    enum?: string;
    items?: SchemaConfig;
    required?: Array<String>;
    additionalProperties?: boolean;
}

export interface EntryPointDataType {
    entryPointType: string,
    userType: string
}
export interface BaseConfiguration  extends PlainObject {
    reservationTime: number;
    totalTimeSimulation: number;
    randomSeed: number;
    boundingBox: {
        northWest: Geo.Point;
        southEast: Geo.Point;
    }
    map: string;
    historyOutputPath: string;
    entryPoints: BaseEntryPoint;
    stations: BaseStation;
}

export interface BaseEntryPoint extends PlainObject {
    userType: string;
}

export interface BaseEntity {
    id: number;
}

export interface BaseStation extends PlainObject, BaseEntity {
    bikes: number | Bike[];
}

export interface Bike extends PlainObject, BaseEntity {}
