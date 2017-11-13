const { sNumber, sObject, sConst } = require('../util/jsonschema/types');
const { sOr, sUnion } = require('../util/jsonschema/operators');
const { xMin, required, requireAll } = require('../util/jsonschema/constraints');
const { UInt, GeoPoint, UserType } = require('../util/commontypes');

const Distributions = [
    sObject({
        type: sConst('RANDOM')
    }, requireAll()),
    sObject({
        type: sConst('POISSON'),
        lambda: sNumber()
    }, requireAll()),
];

const ItemBase = sObject({
    userType: UserType,
    position: GeoPoint,
}, requireAll());

const Items = [
    sUnion(ItemBase, sObject({
        timeInstant: UInt
    }, requireAll())),
    sUnion(ItemBase, sObject({
        distribution: sOr(...Distributions),
        radius: sNumber(xMin(0)),
        timeRange: sObject({
            start: UInt,
            end: UInt,
        }),
    }, required('distribution'))),
];

module.exports = sOr(...Items);