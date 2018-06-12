import {sConst, sEnum, sObject} from "json-schema-builder-ts/dist/types";
import {sAnyOf} from "json-schema-builder-ts/dist/operators/schematical";
import {GeoPoint, UInt} from "./index";
import {typeParameters} from "../schemas-and-form-definitions/users-config";

export const UserType = sEnum(...Object.keys(typeParameters));

export const UserProperties = sAnyOf(...Object.entries(typeParameters).map((user) => {
    const type = user[0];
    const parameters: any = user[1];
    return sObject({
        typeName: sConst(type),
        parameters: parameters && sObject(parameters)
    });
}));

export const SingleUser = sObject({
    userType: UserProperties,
    position: GeoPoint,
    timeInstant: UInt
});