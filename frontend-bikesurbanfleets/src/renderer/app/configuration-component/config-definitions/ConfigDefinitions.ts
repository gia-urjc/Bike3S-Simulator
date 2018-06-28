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
        `;
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
            </div>`;
        }
        return popUp;
    }
}

export interface FormJsonSchema {
    schema: any;
    data: any;
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
