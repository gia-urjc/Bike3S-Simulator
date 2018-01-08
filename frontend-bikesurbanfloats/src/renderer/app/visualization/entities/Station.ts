import { DivIcon } from 'leaflet';
import { Geo } from '../../../../shared/util';
import { Bike } from './Bike';
import { Entity, Historic } from './Entity';

import './station.css';

@Historic<Station>({
    jsonIdentifier: 'stations',
    show: {
        at: (station) => station.position,
        icon: (station) => {
            const nBikes = station.bikes.reduce((r, v) => v !== null && r + 1 || r, 0);
            const slotRatio = (station.capacity - nBikes) / station.capacity * 100;
            const gradient = new ConicGradient({
                stops: `tomato ${slotRatio}%, mediumseagreen 0`,
                size: 30,
            });
            return new DivIcon({
                className: 'station-marker',
                iconSize: [30, 30],
                html: `
                    <div class="ratio-ring" style="background: url(${gradient.png}) no-repeat;">
                        <div class="bike-counter">${nBikes}</div>
                    </div>
                `,
            });
        },
        onMarkerEvent: {
            click: (station) => console.log(station)
        },
        popup: (station) => `
            <div>
                <strong>Station #${station.id}</strong>
            </div>
            <div>Capacity: ${station.capacity}</div>
        `,
    },
})
export class Station extends Entity {
    position: Geo.Point;
    capacity: number;
    bikes: Array<Bike | null>;
}
