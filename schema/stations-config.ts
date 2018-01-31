import {options} from "./common";
import {sArray, sObject} from "json-schema-builder-ts/dist/types";
import {Station} from "./common-config";
import {JsonSchema} from "json-schema-builder-ts";

export default new JsonSchema(options, sObject({
    stations: sArray(Station),
}).require.all().restrict());