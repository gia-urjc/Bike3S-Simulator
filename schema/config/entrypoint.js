const { SNumber, SObject, SConst } = require('../util/jsonschema/types');
const { SOr, SUnion } = require('../util/jsonschema/operators');
const { XMin, Require, RequireAll } = require('../util/jsonschema/constraints');
const { UInt, GeoPoint, UserType } = require('../util/commontypes');

const Distributions = [
    SObject({
        type: SConst('RANDOM')
    }, RequireAll()),
    SObject({
        type: SConst('POISSON'),
        lambda: SNumber()
    }, RequireAll()),
];

const ItemBase = SObject({
    userType: UserType,
    position: GeoPoint,
}, RequireAll());

const Items = [
    SUnion(ItemBase, SObject({
        timeInstant: UInt
    }, RequireAll())),
    SUnion(ItemBase, SObject({
        distribution: SOr(...Distributions),
        radius: SNumber(XMin(0)),
        timeRange: SObject({
            start: UInt,
            end: UInt,
        }),
    }, Require('distribution'))),
];

module.exports = SOr(...Items);