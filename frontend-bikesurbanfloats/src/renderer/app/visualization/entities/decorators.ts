import { HistoryEntity } from '../../../../shared/history';
import { Route } from '../../../../shared/util';
import { Entity } from './Entity';

export function JsonIdentifier(identifier: string) {
    return function <E extends Entity, J extends HistoryEntity> (Target: { new(json: J): E }) {
        Reflect.defineMetadata(JsonIdentifier, identifier, Target);
        return Target;
    }
}

type EntityCallback<T> = (entity: any) => T;

export interface VisualOptions {
    show?: boolean | EntityCallback<boolean>,
    moveAlong?: EntityCallback<Route | null>,
    speed?: EntityCallback<number>,
}

export function VisualEntity(options: VisualOptions) {
    return function <E extends Entity, J extends HistoryEntity> (Target: { new(json: J): E }) {
        Reflect.defineMetadata(VisualEntity, options, Target);
        return Target;
    }
}
