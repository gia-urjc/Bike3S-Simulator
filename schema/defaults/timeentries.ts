import { JsonSchema } from 'json-schema-builder-ts';
import idReference from '../common/idreference';
import { sArray, sInteger, sObject, sNull, sEnum, sString } from 'json-schema-builder-ts/dist/types';
import { options, UInt } from '../common/index';
import {sAnyOf} from "json-schema-builder-ts/dist/operators/schematical";

const PropertyChange = sObject({
    old: {},
    new: {},
});

const ResultTypes = sEnum(
        'FAILED_BIKE_RENTAL', 'SUCCESSFUL_BIKE_RENTAL', 'FAILED_BIKE_RETURN', 'SUCCESSFUL_BIKE_RETURN', 
        'FAILED_BIKE_RESERVATION', 'SUCCESSFUL_BIKE_RESERVATION',
        'FAILED_SLOT_RESERVATION', 'SUCCESSFUL_SLOT_RESERVATION', 'SUCCESS', 'FAIL',
        'EXIT_AFTER_APPEARING', 
        'EXIT_AFTER_FAILED_BIKE_RESERVATION', 'EXIT_AFTER_FAILED_BIKE_RENTAL', 'EXIT_AFTER_RESERVATION_TIMEOUT',
        'EXIT_AFTER_REACHING_DESTINATION');
    

const EntityChanges = sObject({
    id: UInt,
}).additionalProperties(PropertyChange);

const EntityDescription = sObject({
    id: UInt,
}).additionalProperties(true);

const idreference = sObject({
    type: sString(),
    id: UInt,
}).require.all().restrict();


const EventEntry = sObject({
    name: sString(),
    order: UInt,
    result: ResultTypes,
    involvedEntities: sAnyOf(sArray(idreference), sNull()),
    newEntities: sAnyOf(sObject().additionalProperties(sArray(EntityDescription)), sNull()),
    changes: sAnyOf(sObject().additionalProperties(sArray(EntityChanges)), sNull()),
    oldEntities: sAnyOf(sObject().additionalProperties(sArray(EntityDescription)), sNull()),
}).require('name', 'result','order','involvedEntities').restrict();

const TimeEntry = sObject({
    time: sInteger(),
    events: sArray(EventEntry),
}).require.all().restrict();

export default new JsonSchema(options, sArray(TimeEntry));
