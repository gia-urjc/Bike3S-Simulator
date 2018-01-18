import {sArray, sInteger, sNull, sObject} from 'json-schema-builder-ts/dist/types';
import {sAnyOf} from 'json-schema-builder-ts/dist/operators/schematical';
import {GeoPoint, UInt} from '../common';
import {rData} from 'json-schema-builder-ts/dist/references';
import {Bike} from './common-config';

export default sObject({
    position: GeoPoint,
    capacity: UInt,
    bikes: sAnyOf(
        sInteger().min(0).max(rData('1/capacity')),
        sArray(sAnyOf(Bike, sNull())).min(rData('1/capacity')).max(rData('1/capacity'))
    ),
}).require.all().restrict();