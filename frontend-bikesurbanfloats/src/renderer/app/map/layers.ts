import { tileLayer } from 'leaflet';
import { settingsPathGenerator } from '../../../shared/settings';
import { LayerEntry, SettingsLayerPath } from './util';

const path = settingsPathGenerator();

export class OpenStreetMap extends LayerEntry {
    static readonly COPYRIGHT = `&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>`;

    constructor() {
        super();
        this.name = 'Open Street Map';
        this.layer = tileLayer('http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: OpenStreetMap.COPYRIGHT,
            maxZoom: 18,
        });
    }
}

@SettingsLayerPath(path.layers.thunderforest())
export class ThunderforestOpenCycleMap extends LayerEntry {
    static readonly COPYRIGHT = `&copy; <a href="http://www.thunderforest.com/">Thunderforest</a>`;

    constructor(key: string) {
        super();
        this.name = 'Thunderforest Open Cycle Map';
        this.layer = tileLayer('http://{s}.tile.thunderforest.com/cycle/{z}/{x}/{y}.png?apikey={apikey}', {
            attribution: `${ThunderforestOpenCycleMap.COPYRIGHT}, ${OpenStreetMap.COPYRIGHT}`,
            maxZoom: 22,
            apikey: key,
        });
    }
}

@SettingsLayerPath(path.layers.mapbox())
export class MapboxStreets extends LayerEntry {
    static readonly CC_BY_SA = `<a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>`;
    static readonly COPYRIGHT = `&copy; <a href="http://mapbox.com">Mapbox</a>`;

    constructor(key: string) {
        super();
        this.name = 'Mapbox Streets';
        this.layer = tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token={accessToken}', {
            attribution: `Map data ${OpenStreetMap.COPYRIGHT}, ${MapboxStreets.CC_BY_SA}, Imagery ${MapboxStreets.COPYRIGHT}`,
            maxZoom: 18,
            id: 'mapbox.streets',
            accessToken: key,
        });
    }
}
