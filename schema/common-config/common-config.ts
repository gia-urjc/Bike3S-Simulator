import {sArray, sConst, sInteger, sNull, sNumber, sObject} from 'json-schema-builder-ts/dist/types';
import {sAnyOf} from 'json-schema-builder-ts/dist/operators/schematical';
import {GeoPoint, UInt} from '../common';
import {rData} from 'json-schema-builder-ts/dist/references';

export const Bike = sObject();

export const Station = sObject({
    position: GeoPoint,
    capacity: UInt,
    bikes: sAnyOf(
        sInteger().min(0).max(rData('1/capacity')),
        sArray(sAnyOf(Bike, sNull())).min(rData('1/capacity')).max(rData('1/capacity'))
    ),
}).require.all().restrict();

export const Percentage = sNumber().min(0).max(100);