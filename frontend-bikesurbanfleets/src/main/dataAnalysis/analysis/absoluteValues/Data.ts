import { PlainObject } from "../../../../shared/util";

export interface Data extends PlainObject {
  actionType: string;
  entityType: string;
}