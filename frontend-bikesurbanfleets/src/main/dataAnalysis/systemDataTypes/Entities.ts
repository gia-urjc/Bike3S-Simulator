import { PlainObject } from '../../../shared/util';
export interface Entity extends  PlainObject {
    id: number;
}

export interface User extends Entity {}

export interface Station extends Entity {
    capacity: number;
    availablebikes: number;
    reservedbikes: number;
    reservedslots: number;
    availableslots: number;
}
