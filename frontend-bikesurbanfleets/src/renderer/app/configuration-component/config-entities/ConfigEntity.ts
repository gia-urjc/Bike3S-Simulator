import {Icon, LeafletEvent} from "leaflet";
import {Geo} from "../../../../shared/util";
import {LeafletUtil} from "../../visualization-component/util";
import {Flatten} from "../../../../shared/mappedtypes";

type EntityCallback<T extends ConfigEntity, R> = (entity: T) => R;
type LeafletEventCallback<T extends ConfigEntity, E extends LeafletEvent> = (entity: T, event: E) => void;
type AllowedEvents = Flatten<LeafletUtil.MarkerEvents, 'Map' | 'Mouse' | 'Popup' | 'Tooltip'>;

export interface IEntityConf<T extends ConfigEntity = any> {
    marker?: {
        icon?: EntityCallback<T, Icon<any>>,
        popup?: EntityCallback<T, string>,
        on?: {
            [P in keyof AllowedEvents]?: LeafletEventCallback<T, AllowedEvents[P]>
        },
    }
}

export function MapProperties<T extends ConfigEntity>(configuration: IEntityConf<T>) {
    return function (Target: { new(): T }) {
        Reflect.defineMetadata(MapProperties, configuration, Target);
        return Target;
    }
}

export abstract class ConfigEntity {

    position: Geo.Point | null;

    constructor(position?: Geo.Point) {
        this.position = position || null;
    }
}
