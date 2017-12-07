import { tileLayer, TileLayer } from 'leaflet';

export abstract class LayerEntry {
    constructor(public name: string, public layer: TileLayer) {}
}

export class OpenStreetMap extends LayerEntry {
    static readonly ATTRIBUTION = `&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>`;

    constructor() {
        super(
            'Open Street Map',
            tileLayer('http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                attribution: OpenStreetMap.ATTRIBUTION,
                maxZoom: 18,
            })
        );
    }
}

export class ThunderforestOpenCycleMap extends LayerEntry {
    static readonly ATTRIBUTION = `&copy; <a href="http://www.thunderforest.com/">Thunderforest</a>`;

    constructor(apikey: string) {
        super(
            'Thunderforest Open Cycle Map',
            tileLayer('http://{s}.tile.thunderforest.com/cycle/{z}/{x}/{y}.png?apikey={apikey}', {
                attribution: `${ThunderforestOpenCycleMap.ATTRIBUTION}, ${OpenStreetMap.ATTRIBUTION}`,
                maxZoom: 22,
                apikey: apikey,
            })
        );
    }
}

export class MapboxStreets extends LayerEntry {
    static readonly CC_BY_SA = `<a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>`;
    static readonly ATTRIBUTION = `&copy; <a href="http://mapbox.com">Mapbox</a>`;

    constructor(accessToken: string) {
        super(
            'Mapbox Streets',
            tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token={accessToken}', {
                attribution: `Map data ${OpenStreetMap.ATTRIBUTION}, ${MapboxStreets.CC_BY_SA}, Imagery ${MapboxStreets.ATTRIBUTION}`,
                maxZoom: 18,
                id: 'mapbox.streets',
                accessToken: accessToken,
            })
        );
    }
}
