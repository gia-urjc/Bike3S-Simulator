import { PlainObject } from "../../../../shared/util";
import { Entity } from "../../systemDataTypes/Entities";
import { Data } from "./Data";

export interface SystemInfo {
    basicData: any;
    data: Data;
    getData(): Data;
    init(): Promise<void>;
}