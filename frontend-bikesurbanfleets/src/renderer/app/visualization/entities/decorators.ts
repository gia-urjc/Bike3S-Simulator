import { Icon } from 'leaflet';
import { GeoPoint, Route } from '../../../../shared/util';
import { Entity } from './Entity';

export function JsonIdentifier(identifier: string) {
    return function <E extends Entity> (Target: { new(): E }) {
        Reflect.defineMetadata(JsonIdentifier, identifier, Target);
        return Target;
    }
}

type EntityCallback<T> = (entity: any) => T;

export interface VisualOptions {
    showAt: EntityCallback<GeoPoint | null>,
    icon?: EntityCallback<Icon<any>>,
    move?: {
        route: EntityCallback<Route | null>,
        speed: EntityCallback<number>,
    }
}

export function VisualEntity(options: VisualOptions) {
    return function <E extends Entity> (Target: { new(): E }) {
        Reflect.defineMetadata(VisualEntity, options, Target);
        return Target;
    }
}
