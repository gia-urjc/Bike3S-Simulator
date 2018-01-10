import { HistoryEntity } from "../../../shared/history";
import { PlainObject } from '../../../shared/util';
export interface Entity extends  PlainObject {
    id: number;
}

export interface User extends Entity {
    
}

export interface Station extends Entity {
    capacity: number;
    reservedBikes: number;
    reservedSlots: number;
    
}

export enum ReservationType {
    BIKE, SLOT
}

export enum ReservationState {
    FAILED, SUCCESSFUL
}

export interface Reservation extends Entity {
    type: string;
    state: string;
}