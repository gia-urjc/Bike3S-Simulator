import { PlainObject } from "../../../../shared/util";

export interface Data extends PlainObject {
  getFactType(): string;
  getEntityType(): string;
}