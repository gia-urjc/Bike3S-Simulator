import { Marker } from 'leaflet';
import { HistoryEntity } from '../../../../shared/history';
import { GeoPoint, Route } from '../../../../shared/util';
import { Entity } from './Entity';

export function JsonIdentifier(identifier: string) {
    return function <E extends Entity, J extends HistoryEntity> (Target: { new(json: J): E }) {
        Reflect.defineMetadata(JsonIdentifier, identifier, Target);
        return Target;
    }
}

type EntityCallback<T> = (entity: any) => T;

export interface VisualOptions {
    showAt: EntityCallback<GeoPoint | null>,
    moveAlong?: EntityCallback<Route | null>,
    speed?: EntityCallback<number>,
    onChange?: (entity: any, marker: Pick<Marker, 'setIcon'>) => void,
}

export function VisualEntity(options: VisualOptions) {
    return function <E extends Entity, J extends HistoryEntity> (Target: { new(json: J): E }) {
        Reflect.defineMetadata(VisualEntity, options, Target);
        return Target;
    }
}
