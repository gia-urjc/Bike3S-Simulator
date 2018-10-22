import { sEnum, sObject, sConst } from "json-schema-builder-ts/dist/types";
import { sAnyOf } from "json-schema-builder-ts/dist/operators/schematical";
import { typeParameters } from "../schemas-and-form-definitions/recom-config";

export const RecomType = sEnum(...Object.keys(typeParameters));

export const RecomProperties = sAnyOf(...Object.entries(typeParameters).map((recom) => {
    const type = recom[0];
    const parameters: any = recom[1];
    return sObject({
        typeName: sConst(type),
        parameters: parameters && sObject(parameters)
    });
}));