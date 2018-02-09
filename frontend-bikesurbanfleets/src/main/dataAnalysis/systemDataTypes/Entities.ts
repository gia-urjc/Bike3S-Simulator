import { HistoryEntity } from "../../../shared/history";
import { PlainObject } from '../../../shared/util';
export interface Entity extends  PlainObject {
    id: number;
}

export interface User extends Entity {}

export interface Station extends Entity {
    capacity: number;
}

export interface Reservation extends Entity {
    type: string;
    state: string;
}