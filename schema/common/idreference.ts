import { SchemaBuilder } from 'json-schema-builder-ts/dist/core/builder';
import { sConst, sObject } from 'json-schema-builder-ts/dist/types';
import { UInt } from './index';

export default (type: string, id: SchemaBuilder = UInt) => sObject({
    type: sConst(type),
    id: id
}).require.all().restrict();
