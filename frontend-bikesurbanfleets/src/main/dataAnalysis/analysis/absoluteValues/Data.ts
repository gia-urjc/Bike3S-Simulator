import { PlainObject } from "../../../../shared/util";
import { Observer } from '../ObserverPattern';

export interface Data extends PlainObject, Observer {
  getFactType(): string;
  getEntityType(): string;
}