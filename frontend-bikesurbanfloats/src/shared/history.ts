import { isPlainObject } from 'lodash';
import { PlainObject } from './util';

export interface HistoryEntity extends PlainObject {
    id: number,
}

export interface PropertyChange<T> {
    old: T,
    new: T,
}

export interface HistoryEntitiesJson {
    prototype: Array<string>,
    instances: Array<HistoryEntity>,
}

export type EntityChanges = {
    id: number,
} & {
    [key: string]: PropertyChange<any>
}

export type HistoryTimeEntry = {
    time: number,
    events: Array<{
        name: string,
        changes: {
            [key: string]: Array<EntityChanges>
        }
    }>
}

export interface IdReference {
    type: string,
    id: number | Array<number | null>,
}

export function isIdReference(property: any): boolean {
    return isPlainObject(property) && 'type' in property && 'id' in property;
}
