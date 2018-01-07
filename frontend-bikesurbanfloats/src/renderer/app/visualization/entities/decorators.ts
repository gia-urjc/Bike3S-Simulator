import { Icon, LeafletEvent } from 'leaflet';
import { Flatten } from '../../../../shared/mappedtypes';
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

type AllowedEvents = Flatten<LeafletUtil.MarkerEvents, 'Map' | 'Mouse' | 'Popup' | 'Tooltip'>;

export interface VisualOptions<T extends Entity = any> {
    show: EntityCallback<T, Geo.Point | null> | {
        when: EntityCallback<T, boolean>,
        route: EntityCallback<T, Geo.Route | null>,
        speed: EntityCallback<T, number>,
    },
    icon?: EntityCallback<T, Icon<any>>,
    popup?: EntityCallback<T, string>,
    onMarkerEvent?: {
        [P in keyof AllowedEvents]?: LeafletEventCallback<T, AllowedEvents[P]>
    },
    onChange?: {
        [P in keyof T]?: EntityCallback<T, void>
    },
}

interface Future<T extends Entity = any> {
    jsonIdentifier: string,
    show?: {
        when: EntityCallback<T, boolean>,
        at: EntityCallback<T, Geo.Point | null> | {
            route: EntityCallback<T, Geo.Route | null>,
            speed: EntityCallback<T, number>,
        },
        icon?: EntityCallback<T, Icon<any>>,
        popup?: EntityCallback<T, string>,
        onMarkerEvent?: {
            [P in keyof AllowedEvents]?: LeafletEventCallback<T, AllowedEvents[P]>
        },
    },
    onChange?: {
        [P in keyof T]?: EntityCallback<T, void>
    },
}

export function VisualEntity<T extends Entity>(options: VisualOptions<T>) {
    return function (Target: { new(): T }) {
        Reflect.defineMetadata(VisualEntity, options, Target);
        return Target;
    }
}
