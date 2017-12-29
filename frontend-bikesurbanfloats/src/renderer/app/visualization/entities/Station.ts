import { DivIcon } from 'leaflet';
import { GeoPoint } from '../../../../shared/util';
import { Bike } from './Bike';
import { JsonIdentifier, VisualEntity } from './decorators';
import { Entity } from './Entity';

import './station.css';

@JsonIdentifier('stations')
@VisualEntity({
    showAt: (station: Station) => station.position,
    icon: (station: Station) => {
        const bikes = station.bikes.reduce((r, v) => v !== null && r + 1 || r, 0);
        const slotRatio = (station.capacity - bikes) / station.capacity * 100;
        const circle = new ConicGradient({
            stops: `tomato ${slotRatio}%, mediumseagreen 0`,
            size: 30,
        });
        return new DivIcon({
            className: 'station-marker',
            iconSize: [30, 30],
            html: `
            <div class="ratio-ring" style="background: url(${circle.png}) no-repeat;">
                <div class="bike-counter">${bikes}</div>
            </div>
            `,
        });
    }
})
export class Station extends Entity {
    position: GeoPoint;
    capacity: number;
    bikes: Array<Bike | null>;
}
