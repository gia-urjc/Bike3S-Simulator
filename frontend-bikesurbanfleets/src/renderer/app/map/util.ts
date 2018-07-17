import { TileLayer } from 'leaflet';

export abstract class LayerEntry {
    static readonly SettingsPathKey = Symbol('settings-path');
    name: string;
    layer: TileLayer;
}

export function SettingsLayerPath(path: string) {
    return function (Target: { new(key: string): LayerEntry }) {
        Reflect.defineMetadata(LayerEntry.SettingsPathKey, path, Target);
    };
}
