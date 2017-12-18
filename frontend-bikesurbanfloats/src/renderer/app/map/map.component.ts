import { Component, Inject, Input, OnInit } from '@angular/core';
import { Control, latLng, MapOptions, Marker } from 'leaflet';
import { SettingsLayerEntry } from '../../../shared/settings/definitions';
import { AjaxProtocol } from '../../ajax/AjaxProtocol';
import * as layers from './layers';
import { LayerEntry } from './util';

@Component({
    selector: 'map-view',
    template: require('./map.component.html'),
})
export class MapComponent implements OnInit {
    layerControl: { baseLayers: Control.LayersObject };
    options: MapOptions;

    @Input() markers: Array<Marker> = [];

    constructor(@Inject('AjaxProtocol') private ajax: AjaxProtocol) {}

    ngOnInit() {
        const osm = new layers.OpenStreetMap();

        this.options = {
            layers: [osm.layer],
            zoom: 15,
            center: latLng(40.72, -74.04), // TODO: make customizable
        };

        this.layerControl = {
            baseLayers: {
                [osm.name]: osm.layer
            }
        };

        this.loadLayers();
    }

    private async loadLayers(): Promise<void> {
        for (let LayerConstructor of Object.values(layers)) {
            const constructorArguments = [];

            if (Reflect.hasOwnMetadata(LayerEntry.SettingsPathKey, LayerConstructor)) {
                const propertyPath = Reflect.getOwnMetadata(LayerEntry.SettingsPathKey, LayerConstructor);
                const settingsLayer: SettingsLayerEntry = await this.ajax.settings.get(propertyPath);

                if (!settingsLayer.enabled) continue;

                constructorArguments.push(settingsLayer.key);
            }

            const map: LayerEntry = Reflect.construct(LayerConstructor, constructorArguments);

            if (map.name in this.layerControl.baseLayers) continue;

            this.layerControl.baseLayers[map.name] = map.layer;
        }
    }
}
