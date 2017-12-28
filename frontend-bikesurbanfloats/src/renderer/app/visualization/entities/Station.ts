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
    onChange: (station: Station, marker) => {
        const bikes = station.bikes.reduce((r, v) => v !== null && r + 1 || r, 0);
        const circle = new ConicGradient({
            stops: `red ${(station.capacity - bikes) / station.capacity * 100}%, green 0`,
            size: 50,
        });
        marker.setIcon(new DivIcon({
            html: `
            <div class="station outer" style="background: url(${circle.png}) no-repeat;">
                <div class="station inner">${bikes}</div>
            </div>
            `,
            className: '',
            iconSize: [50, 50],
        }));
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
