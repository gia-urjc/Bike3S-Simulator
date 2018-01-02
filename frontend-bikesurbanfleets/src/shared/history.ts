import { PlainObject } from './util';

export interface HistoryEntity extends PlainObject {
    id: number,
}

export interface PropertyChange<T> {
    old: T,
    new: T,
}

export interface HistoryEntities {
    [key: string]: Array<HistoryEntity>
}

export type EntityChanges = {
    id: number,
} & {
    [key: string]: PropertyChange<any>
}

export type HistoryTimeEntries = Array<{
    time: number,
    events: Array<{
        name: string,
        changes: {
            [key: string]: Array<EntityChanges>
        }
    }>
}>
