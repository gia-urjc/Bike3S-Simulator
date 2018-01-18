import EntryPoint from './entry-point'
import Station from './station'
import {sNumber, sObject} from 'json-schema-builder-ts/dist/types';

const Bike = sObject();


export {
    EntryPoint,
    Bike,
    Station
}