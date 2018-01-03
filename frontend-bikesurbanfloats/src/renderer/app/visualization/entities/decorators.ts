import { Icon, LeafletEvent } from 'leaflet';
import { Geo } from '../../../../shared/util';
import { LeafletUtil } from '../util';
import { Entity } from './Entity';

export function JsonIdentifier(identifier: string) {
    return function <E extends Entity> (Target: { new(): E }) {
        Reflect.defineMetadata(JsonIdentifier, identifier, Target);
        return Target;
    }
}

type EntityCallback<T extends Entity, R> = (entity: T) => R;
type LeafletEventCallback<T extends Entity, E extends LeafletEvent> = (entity: T, event: E) => void;

type MouseEvents = LeafletUtil.MarkerEvents['Mouse'];

export interface VisualOptions<T extends Entity = any> {
    show: EntityCallback<T, Geo.Point | null> | {
        when: EntityCallback<T, boolean>,
        route: EntityCallback<T, Geo.Route | null>,
        speed: EntityCallback<T, number>,
    },
    icon?: EntityCallback<T, Icon<any>>,
    onAction?: {
        [P in keyof MouseEvents]?: LeafletEventCallback<T, MouseEvents[P]>
    },
    onChange?: {
        [P in keyof T]?: EntityCallback<T, void>
    }
}

export function VisualEntity<T extends Entity>(options: VisualOptions<T>) {
    return function (Target: { new(): T }) {
        Reflect.defineMetadata(VisualEntity, options, Target);
        return Target;
    }
}
