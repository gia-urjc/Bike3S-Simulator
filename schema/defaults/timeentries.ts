import { JsonSchema } from 'json-schema-builder-ts';
import idReference from '../common/idreference';
import { sArray, sInteger, sObject, sNull, sEnum, sString } from 'json-schema-builder-ts/dist/types';
import { options, UInt } from '../common/index';
import {sAnyOf} from "json-schema-builder-ts/dist/operators/schematical";

const PropertyChange = sObject({
    old: {},
    new: {},
});

const EventTypes = sEnum('USER_EVENT', 'MANAGER_EVENT');

const ResultTypes = sEnum('SUCCESS', 'FAIL');

const AdditionalInfo = sEnum(
        'EXIT_AFTER_APPEARING', 
        'EXIT_AFTER_FAILED_BIKE_RESERVATION', 'EXIT_AFTER_FAILED_BIKE_RENTAL', 'EXIT_AFTER_RESERVATION_TIMEOUT',
        'EXIT_AFTER_REACHING_DESTINATION', 'RETRY_EVENT');
    

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
    type: EventTypes,
    order: UInt,
    result: ResultTypes,
    info: sAnyOf(AdditionalInfo,sNull()),
    involvedEntities: sAnyOf(sArray(idreference), sNull()),
    newEntities: sAnyOf(sObject().additionalProperties(sArray(EntityDescription)), sNull()),
    changes: sAnyOf(sObject().additionalProperties(sArray(EntityChanges)), sNull()),
    oldEntities: sAnyOf(sObject().additionalProperties(sArray(EntityDescription)), sNull()),
}).require('name', 'result','order','involvedEntities', 'type').restrict();

const TimeEntry = sObject({
    time: sInteger(),
    events: sArray(EventEntry),
}).require.all().restrict();

export default new JsonSchema(options, sArray(TimeEntry));
