import { DivIcon } from 'leaflet';
import { GeoPoint } from '../../../../shared/util';
import { Bike } from './Bike';
import { JsonIdentifier, VisualEntity } from './decorators';
import { Entity } from './Entity';

import './station.css';

interface JsonStation {
    id: number,
    position: GeoPoint,
    capacity: number,
    bikes: {
        type: 'bikes',
        id: Array<Bike | null>
    },
}

@JsonIdentifier('stations')
@VisualEntity({
    showAt: (station: Station) => station.position,
    icon: (station: Station) => {
        const bikes = station.bikes.reduce((r, v) => v !== null && r + 1 || r, 0);
        const slotRatio = (station.capacity - bikes) / station.capacity * 100;
        const circle = new ConicGradient({
            stops: `tomato ${slotRatio}%, mediumseagreen 0`,
            size: 50,
        });
        return new DivIcon({
            className: 'station-marker',
            iconSize: [50, 50],
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

    constructor(json: JsonStation) {
        super(json.id);
        this.position = json.position;
        this.capacity = json.capacity;
        this.bikes = json.bikes.id;
    }
}
