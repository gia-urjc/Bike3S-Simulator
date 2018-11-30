import { JsonSchema } from 'json-schema-builder-ts';
import { sArray, sConst, sObject, sString } from 'json-schema-builder-ts/dist/types';
import { options, UInt } from '../common/index';

const EntityInstance = sObject({
    id: UInt,
}).additionalProperties(true);

export default new JsonSchema(options, sObject({
    prototype: sArray(sString()).contains(sConst('id')).unique(true),
    instances: sArray(EntityInstance),
}));
