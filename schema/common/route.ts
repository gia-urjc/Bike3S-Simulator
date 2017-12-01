import { sArray, sNumber, sObject } from 'json-schema-builder-ts/dist/types';
import { GeoPoint } from './index';

export default sObject({
    points: sArray(GeoPoint),
    totalDistance: sNumber().xMin(0)
}).require.all().restrict();
