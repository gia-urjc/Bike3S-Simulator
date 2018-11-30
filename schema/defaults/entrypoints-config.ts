import {sArray, sObject} from "json-schema-builder-ts/dist/types";
import {options} from "../common";
import {JsonSchema} from "json-schema-builder-ts";
import {EntryPoint} from "../schemas-and-form-definitions/entrypoints-config";

export default new JsonSchema(options, sObject({
    entryPoints: sArray(EntryPoint)
}).require.all().restrict());