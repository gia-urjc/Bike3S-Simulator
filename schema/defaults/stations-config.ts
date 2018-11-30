import {sArray, sObject} from "json-schema-builder-ts/dist/types";
import {options} from "../common";
import {JsonSchema} from "json-schema-builder-ts";
import {Station} from "../schemas-and-form-definitions/stations-config";

export default new JsonSchema(options, sObject({
    stations: sArray(Station),
}).require.all().restrict());