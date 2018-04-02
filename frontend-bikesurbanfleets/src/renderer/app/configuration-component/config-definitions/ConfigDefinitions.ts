import {Circle, DivIcon, FeatureGroup, Marker} from "leaflet";
import {PlainObject} from "../../../../shared/util";


export class Station{
    private stationInfo: any;
    private marker: Marker;

    static startIcon() {
        const size = 30;
        const gradient = new ConicGradient({
            stops: `tomato $bue{slotRatio}%, mediumseagreen 0`,
            size: size,
        });
        return new DivIcon({
            className: 'station-marker',
            iconSize: [size, size],
            html: `
                    <div class="ratio-ring" style="background: url(${gradient.png}) no-repeat;">
                        <div class="bike-counter">${0}</div>
                    </div>
                `,
        });
    }

    public constructor(stationsInfo: any, marker: Marker) {
        this.stationInfo = stationsInfo;
        this.marker = marker;
    }

    public getStationInfo(): any {
        return this.stationInfo;
    }

    public getMarker(): Marker {
        return this.marker;
    }

    public getIcon(): DivIcon {
        const size = 30;
        const slotRatio = (this.stationInfo.capacity - this.stationInfo.bikes) / this.stationInfo.capacity * 100;
        const gradient = new ConicGradient({
            stops: `tomato $bue{slotRatio}%, mediumseagreen 0`,
            size: size,
        });
        return new DivIcon({
            className: 'station-marker',
            iconSize: [size, size],
            html: `
                    <div class="ratio-ring" style="background: url(${gradient.png}) no-repeat;">
                        <div class="bike-counter">${this.stationInfo.bikes}</div>
                    </div>
                `,
        });
    }

    public getPopUp(): string {
        return `
            <div>
                <strong>Station #${this.stationInfo.bikes}</strong>
            </div>
            <div>Capacity: ${this.stationInfo.capacity}</div>
        `
    }

}

export class EntryPoint {

    private entryPointInfo: PlainObject;
    private circle: Circle;

    constructor(entryPointInfo: PlainObject, circle: Circle) {
        this.entryPointInfo = entryPointInfo;
        this.circle = circle;
    }

    getEntryPointInfo(): PlainObject {
        return this.entryPointInfo;
    }

    getCircle(): Circle {
        return this.circle;
    }

    getPopUp(): string {
        let popUp = `
            <div>
                <strong>Entry Point</strong>
            </div>
            <div>
                <strong>Type: </strong> ${this.entryPointInfo.entryPointType}
            </div>
            <div>
                <strong>UserType: </strong> ${this.entryPointInfo.userType.typeName}
            </div>`;
        if (this.entryPointInfo.entryPointType === "POISSON") {
            popUp += `
            <div>
                <strong>λ :</strong> ${this.entryPointInfo.distribution.lambda}
            </div>`
        }
        return popUp;
    }
}

export interface FormJsonSchema {
    schema: any,
    data: any
}

export namespace LeafletDrawFunctions {
    export function createLeafletDrawOptions(featureGroup: FeatureGroup) {
        return {
            draw: {
                polyline: false,
                polygon: false,
                rectangle: {
                    shapeOptions: {
                        opacity: 0.4,
                        weight: 4
                    }
                },
                circlemarker: false,
                circle: {
                    shapeOptions: {
                        color: '#e81b1b',
                    }
                },
                marker: {
                    icon: Station.startIcon()
                }
            },
            edit: {
                featureGroup: featureGroup,
                edit: false
            }
        };
    }
}


