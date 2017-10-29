const { JSNumber, JSInteger, JSObject, JSArray, JSEnum, JSNull, JsonSchema } = require('../util/jsonschema');
const { Min, XMin, RequireAll } = require('../util/jsonschema/constraints');
const { GeoPoint, UserType, IdReference } = require('../common');

const User = JSObject({
    id: JSInteger(Min(0)),
    type: UserType,
    walkingVelocity: JSNumber(XMin(0)),
    cyclingVelocity: JSNumber(XMin(0)),
}, RequireAll());

const Bike = JSObject({
    id: JSInteger(Min(0)),
}, RequireAll());

const Station = JSObject({
    id: JSInteger(Min(0)),
    position: GeoPoint,
    capacity: JSInteger(Min(0)),
    bikes: JSArray(IdReference)
}, RequireAll());

const Reservation = JSObject({
    id: JSInteger(Min(0)),
    startTime: JSInteger(Min(0)),
    endTime: JSInteger(Min(0)),
    user: JSInteger(Min(0)),
    station: JSInteger(Min(0)),
    bike: {
        anyOf: [JSInteger(Min(0)), JSNull()]
    },
    type: JSEnum('SLOT', 'BIKE'),
    state: JSEnum('FAILED', 'ACTIVE', 'EXPIRED', 'SUCCESSFUL')
}, RequireAll());

module.exports = JsonSchema(JSObject({
    users: JSArray(User),
    bikes: JSArray(Bike),
    stations: JSArray(Station),
    reservations: JSArray(Reservation)
}, RequireAll()));