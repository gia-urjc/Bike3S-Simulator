import { JsonSchema } from 'json-schema-builder-ts';
import { sArray, sInteger, sObject, sString } from 'json-schema-builder-ts/dist/types';
import { options, UInt } from '../common/index';

const PropertyChange = sObject({
    old: {},
    new: {},
});

const EntityChanges = sObject({
    id: UInt,
}).additionalProperties(PropertyChange);

const EventEntry = sObject({
    name: sString(),
    changes: sObject().additionalProperties(sArray(EntityChanges)),
}).require.all().restrict();

const TimeEntry = sObject({
    time: sInteger(),
    events: sArray(EventEntry),
}).require.all().restrict();

export default new JsonSchema(options, sArray(TimeEntry));
