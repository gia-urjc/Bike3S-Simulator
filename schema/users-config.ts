import {GeoPoint, options, UInt} from "./common";
import {sArray, sBoolean, sInteger, sObject} from "json-schema-builder-ts/dist/types";
import {JsonSchema} from "json-schema-builder-ts";
import {UserProperties} from "./common/users";

const SingleUser = sObject({
    userType: UserProperties,
    position: GeoPoint,
    timeInstant: UInt
});

export default new JsonSchema(options, sObject({
    initialUsers: sArray(SingleUser),
}).require.all().restrict());