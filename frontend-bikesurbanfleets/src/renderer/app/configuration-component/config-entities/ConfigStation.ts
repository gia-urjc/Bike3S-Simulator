import {ConfigEntity, MapProperties} from "./ConfigEntity";
import {DivIcon} from "leaflet";
import {Geo} from "../../../../shared/util";

@MapProperties<ConfigStation>({
    marker: {
        icon: (configStation) => {
            const size = 30;
            const slotRatio = (configStation.capacity - configStation.numBikes) / configStation.capacity * 100;
            const gradient = new ConicGradient({
                stops: `tomato ${slotRatio}%, mediumseagreen 0`,
                size: size,
            });
            return new DivIcon({
                className: 'station-marker',
                iconSize: [size, size],
                html: `
                    <div class="ratio-ring" style="background: url(${gradient.png}) no-repeat;">
                        <div class="bike-counter">${configStation.numBikes}</div>
                    </div>
                `,
            });
        },
        on: {
            click: (station) => console.log(station)
        },
        popup: (station) => `
            <div>
                <strong>Station #${station.numBikes}</strong>
            </div>
            <div>Capacity: ${station.capacity}</div>
        `,
    }
})
export class ConfigStation extends ConfigEntity{
    numBikes: number;
    capacity: number;

    constructor(numBikes?: number, capacity?: number, position?: Geo.Point) {
        super(position);
        this.numBikes = numBikes || 0;
        this.capacity = capacity || 0;
    }
}
