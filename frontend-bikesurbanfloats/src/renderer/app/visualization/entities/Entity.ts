import { Icon, LeafletEvent } from 'leaflet';
import { Flatten } from '../../../../shared/mappedtypes';
import { Geo } from '../../../../shared/util';
import { LeafletUtil } from '../util';

type EntityCallback<T extends Entity, R> = (entity: T) => R;
type LeafletEventCallback<T extends Entity, E extends LeafletEvent> = (entity: T, event: E) => void;
type AllowedEvents = Flatten<LeafletUtil.MarkerEvents, 'Map' | 'Mouse' | 'Popup' | 'Tooltip'>;

export interface VisualConfiguration<T extends Entity = any> {
    jsonIdentifier: string,
    show?: {
        at: EntityCallback<T, Geo.Point | null> | {
            route: EntityCallback<T, Geo.Route | null>,
            speed: EntityCallback<T, number>,
        },
        when?: EntityCallback<T, boolean>,
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

export function Visual<T extends Entity>(configuration: VisualConfiguration<T>) {
    return function (Target: { new(): T }) {
        Reflect.defineMetadata(Visual, configuration, Target);
        return Target;
    }
}

export abstract class Entity {
    id: number;
}
