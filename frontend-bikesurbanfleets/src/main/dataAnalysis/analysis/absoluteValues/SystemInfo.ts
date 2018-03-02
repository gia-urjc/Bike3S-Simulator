import { PlainObject } from "../../../../shared/util";
import { Entity } from "../../systemDataTypes/Entities";
import { Data } from "./Data";

export interface SystemInfo extends PlainObject {
    basicData: any;
    data: Data;
}