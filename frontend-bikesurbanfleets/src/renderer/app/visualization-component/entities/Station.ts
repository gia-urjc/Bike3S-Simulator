import { DivIcon } from 'leaflet';
import { Geo } from '../../../../shared/util';
import { Bike } from './Bike';
import { Entity, Historic } from './Entity';

import './station.css';

@Historic<Station>({
    jsonIdentifier: 'stations',
    marker: {
        at: (station) => station.position,
        icon: (station) => {
            const size = 30;
            const nBikes = station.availablebikes;
            const avBikeRatio = (station.capacity - station.availablebikes) / station.capacity * 100;
            const resBikeRatio = (station.capacity - station.reservedbikes) / station.capacity * 100;
            const avSlotRatio = (station.capacity - station.availableslots) / station.capacity * 100;
            const gradient = new ConicGradient({
       //         stops: `tomato ${avBikeRatio}%, mediumseagreen 0`,
               stops:  `#52BE80 , #145A32 0 ${avBikeRatio}%, #F4D03F 0 ${avBikeRatio+resBikeRatio}%, #7D6608 0 ${avBikeRatio+resBikeRatio+avSlotRatio}%`,
                size: size,
            });
            return new DivIcon({
                className: 'station-marker',
                iconSize: [size, size],
                html: `
                    <div class="ratio-ring" style="background: url(${gradient.png}) no-repeat;">
                        <div class="bike-counter">${nBikes}</div>
                    </div>
                `,
            });
        },
        on: {
            click: (station) => console.log(station)
        },
        popup: (station) => { 
            let st;
            if (station.oficialID) {
                st= `${station.id} (oficial ${station.oficialID})`;
            } else {
                st= `${station.id} (oficial not known)`;
            }
            return `<div><strong>Station #${st}</strong></div>
            <div>Capacity: ${station.capacity}</div>
            <div>AvBikes/ResBikes/AvSlots/ResSlots: ${station.availablebikes}/${station.reservedbikes}/${station.availableslots}/${station.reservedslots}</div>`;
        }
    }
})
export class Station extends Entity {
    position: Geo.Point;
    oficialID: number;
    availablebikes: number;
    reservedbikes: number;
    reservedslots: number;
    availableslots: number;
    capacity: number;
    
//the list of bikes is not used in the passed history data because of space considerations
    bikes: Array<Bike | null>;
}
