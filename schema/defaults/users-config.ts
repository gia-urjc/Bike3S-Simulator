import {sArray, sObject} from "json-schema-builder-ts/dist/types";
import {options} from "../common";
import {JsonSchema} from "json-schema-builder-ts";
import {SingleUser} from "../common/users";

export default new JsonSchema(options, sObject({
    initialUsers: sArray(SingleUser),
}).require.all().restrict());