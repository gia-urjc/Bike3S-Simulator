import { PlainObject } from "../../../shared/util";
import { AbsoluteValue } from "./AbsoluteValue";

export interface Data extends  PlainObject {
    absoluteValues: Map<number, AbsoluteValue>;
}