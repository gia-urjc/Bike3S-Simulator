import { PlainObject } from "../../../../shared/util";
import { AbsoluteValue } from "./AbsoluteValue";

export interface Data extends PlainObject {
    //readonly NAMES: Array<string>;
    absoluteValues: Map<number, AbsoluteValue>;
}