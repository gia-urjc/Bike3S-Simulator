const { SNumber, SObject, SConst, SAny, SMerge } = require('../util/jsonschema');
const { XMin, Require, RequireAll } = require('../util/jsonschema/constraints');
const { UInt, GeoPoint, UserType } = require('../util/customtypes');

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
    SMerge(ItemBase, SObject({
        timeInstant: UInt
    }, RequireAll())),
    SMerge(ItemBase, SObject({
        distribution: SAny(...Distributions),
        radius: SNumber(XMin(0))
    }, Require('distribution'))),
];

module.exports = SAny(...Items);