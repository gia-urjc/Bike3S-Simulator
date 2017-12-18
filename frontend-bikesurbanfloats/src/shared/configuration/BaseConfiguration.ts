import { BaseStation } from '../entities/BaseStation';
import { GeoPoint } from '../util';
import { BaseEntryPoint } from './BaseEntryPoint';

export interface BaseConfiguration {
    [keyStringConf: string]: any;
    reservationTime: number;
    totalTimeSimulation: number;
    randomSeed: number;
    boundingBox: {
        northWest: GeoPoint;
        southEast: GeoPoint;
    }
    map: string;
    historyOutputPath: string;
    entryPoints: BaseEntryPoint;
    stations: BaseStation;
}
