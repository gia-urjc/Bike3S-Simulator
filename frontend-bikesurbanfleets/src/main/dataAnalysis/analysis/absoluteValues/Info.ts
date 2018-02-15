import { PlainObject } from "../../../../shared/util";

export interface Info extends PlainObject {
  getFactType(): string;
  getEntityType(): string;
}