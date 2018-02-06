import { PlainObject } from "../../../../shared/util";

export interface Calculator extends  PlainObject {
  calculate(): void;
}