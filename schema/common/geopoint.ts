import { sNumber, sObject } from 'json-schema-builder-ts/dist/types';

export default sObject({
    latitude: sNumber(),
    longitude: sNumber(),
}).require.all().restrict();
