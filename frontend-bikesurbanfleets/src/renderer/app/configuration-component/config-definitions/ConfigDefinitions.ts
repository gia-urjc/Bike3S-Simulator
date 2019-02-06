import {Circle, DivIcon, FeatureGroup, Marker} from "leaflet";
import {PlainObject} from "../../../../shared/util";

class ConfigurationHtmlIdGenerator {
    
    private static id: number = 0;


    public static generateId(): number {
        let id = this.id;
        this.id++;
        return id;
    }
}

export abstract class ConfigurationEntity {
    protected configurationHtmlId: number;
    protected info: any;

    abstract getPopUp(): string;
    abstract openPopUp(): void;

    getInfo() {
        return this.info;
    }
    constructor(info: any) {
        this.info = info;
        this.configurationHtmlId = ConfigurationHtmlIdGenerator.generateId();
    }

}

export class Station extends ConfigurationEntity {
    
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
        super(stationsInfo);
        this.marker = marker;
    }

    public getMarker(): Marker {
        return this.marker;
    }

    public getIcon(): DivIcon {
        const size = 30;
        const slotRatio = (this.info.capacity - this.info.bikes) / this.info.capacity * 100;
        const gradient = new ConicGradient({
            stops: `tomato ${slotRatio}%, mediumseagreen 0`,
            size: size,
        });
        return new DivIcon({
            className: 'station-marker',
            iconSize: [size, size],
            html: `
                    <div class="ratio-ring" id="entity-${this.configurationHtmlId}" style="background: url(${gradient.png}) no-repeat;">
                        <div class="bike-counter">${this.info.bikes}</div>
                    </div>
                `,
        });
    }

    public getPopUp(): string {
        (<any>window).stationToEdit = this;
        return `
            <div>
                <strong>Station id: ${this.info.id}</strong>
            </div>
            <div>Capacity: ${this.info.capacity}</div>
            <div>Number of bikes: ${this.info.bikes}</div>
            <button onclick='window.angularComponentRef.zone.run(() => {window.angularComponentRef.editStation(window.stationToEdit)});'>Edit</button>
        `;
    }

    updatePopUp(): void {
        (<any>window).stationToEdit = this;
    }

    public openPopUp(): void {
        (<any>window).stationToEdit = this;
        this.getMarker().openPopup();
    }

}

export class EntryPoint extends ConfigurationEntity{

    private circle: Circle;

    constructor(entryPointInfo: PlainObject, circle: Circle) {
        super(entryPointInfo);
        this.circle = circle;
        //Add an id to the circle html element
        (<Element>this.circle.getElement()).setAttribute("id", `entity-${this.configurationHtmlId}`);
    }

    getCircle(): Circle {
        return this.circle;
    }

    getPopUp(): string {
        (<any>window).entryPointToEdit = this;
        let popUp = `
            <div>
                <strong>Entry Point</strong>
            </div>
            <div>
                <strong>Type: </strong> ${this.info.entryPointType}
            </div>
            <div>
                <strong>UserType: </strong> ${this.info.userType.typeName}
            </div>`;
        if (this.info.entryPointType === "POISSON") {
            popUp += `
            <div>
                <strong>λ :</strong> ${this.info.distribution.lambda}
            </div>`;
        }
        popUp += `
            <button onclick='window.angularComponentRef.zone.run(() => {window.angularComponentRef.editEntryPoint(window.entryPointToEdit)});'>Edit</button>
        `;
        return popUp;
    }

    updatePopUp(): void {
        (<any>window).entryPointToEdit = this;
    }

    openPopUp(): void {
        (<any>window).entryPointToEdit = this;
        this.getCircle().openPopup();
    }
}

export namespace LeafletDrawFunctions {
    export function createLeafletDrawOptions(featureGroup: FeatureGroup): any {
        return {
            draw: {
                toolbar: {
                    buttons: {
                        rectangle: 'Create Bounding Box',
                        circle: 'Create Entry Point',
                        marker: 'Create Station'
                    }
                },
                polyline: false,
                polygon: false,
                rectangle: {
                    shapeOptions: {
                        opacity: 1,
                    }
                },
                circlemarker: false,
                circle: {
                    shapeOptions: {
                        opacity: 1,
                        color: '#e81b1b',
                        weight: 2
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

    export function createCustomMessages() {
        return {
            draw: {
                toolbar: {
                    // #TODO: this should be reorganized where actions are nested in actions
                    // ex: actions.undo  or actions.cancel
                    actions: {
                        title: 'Cancel drawing',
                        text: 'Cancel'
                    },
                    finish: {
                        title: 'Finish drawing',
                        text: 'Finish'
                    },
                    undo: {
                        title: 'Delete last point drawn',
                        text: 'Delete last point'
                    },
                    buttons: {
                        polyline: 'Draw a polyline',
                        polygon: 'Draw a polygon',
                        rectangle: 'Create an area for the simulation',
                        circle: 'Create an entry point',
                        marker: 'Create a station',
                        circlemarker: 'Draw a circlemarker'
                    }
                },
                handlers: {
                    circle: {
                        tooltip: {
                            start: 'Click and drag to draw an entry point.'
                        },
                        radius: 'Radius'
                    },
                    circlemarker: {
                        tooltip: {
                            start: 'Click map to place circle marker.'
                        }
                    },
                    marker: {
                        tooltip: {
                            start: 'Click map to place a station.'
                        }
                    },
                    polygon: {
                        tooltip: {
                            start: 'Click to start drawing shape.',
                            cont: 'Click to continue drawing shape.',
                            end: 'Click first point to close this shape.'
                        }
                    },
                    polyline: {
                        error: '<strong>Error:</strong> shape edges cannot cross!',
                        tooltip: {
                            start: 'Click to start drawing line.',
                            cont: 'Click to continue drawing line.',
                            end: 'Click last point to finish line.'
                        }
                    },
                    rectangle: {
                        tooltip: {
                            start: 'Click and drag to draw an area for the simulation (Bounding Box).'
                        }
                    },
                    simpleshape: {
                        tooltip: {
                            end: 'Release mouse to finish drawing.'
                        }
                    }
                }
            },
            edit: {
                toolbar: {
                    actions: {
                        save: {
                            title: 'Save changes',
                            text: 'Save'
                        },
                        cancel: {
                            title: 'Cancel editing, discards all changes',
                            text: 'Cancel'
                        },
                        clearAll: {
                            title: 'Clear all layers',
                            text: 'Clear All'
                        }
                    },
                    buttons: {
                        edit: 'Edit entities',
                        editDisabled: 'No entities to edit',
                        remove: 'Delete entities',
                        removeDisabled: 'No entities to delete'
                    }
                },
                handlers: {
                    edit: {
                        tooltip: {
                            text: 'Drag handles or markers to edit entities.',
                            subtext: 'Click cancel to undo changes.'
                        }
                    },
                    remove: {
                        tooltip: {
                            text: 'Click on an entity to remove.'
                        }
                    }
                }
            }
        };
    }
}
