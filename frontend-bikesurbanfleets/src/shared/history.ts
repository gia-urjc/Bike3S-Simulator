import { isPlainObject } from 'lodash';
import { PlainObject } from './util';

export interface HistoryEntity extends PlainObject {
    id: number;
}

export interface PropertyChange<T> {
    old: T;
    new: T;
}

export interface HistoryEntitiesJson {
    prototype: Array<string>;
    instances: Array<HistoryEntity>;
}

export type EntityChanges = {
    id: number,
} & {
    [key: string]: PropertyChange<any>
};

export type EntityDescriptions = {
    id: number,
} & {
    [key: string]: <any>
};

export type HistoryTimeEntry = {
    time: number,
    events: Array<{
        name: string,
        order: number,
        newEntities: {
            [key: string]: Array<EntityDescriptions>
        },
        changes: {
            [key: string]: Array<EntityChanges>
        },
        oldEntities: {
            [key: string]: Array<EntityDescriptions>
        },
    }>
};

export enum ReservationType {
    SLOT = 'SLOT',
    BIKE = 'BIKE',
}

export enum ReservationState {
    ACTIVE = 'ACTIVE',
    FAILED = 'FAILED',
    EXPIRED = 'EXPIRED',
    SUCCESSFUL = 'SUCCESSFUL',
}

export interface IdReference {
    type: string;
    id: number | Array<number | null>;
}

export function isIdReference(property: any): boolean {
    return isPlainObject(property) && 'type' in property && 'id' in property;
}
