import {Circle, Marker} from "leaflet";
import {EntryPoint, Station} from "./config-definitions";

export class ConfigurationUtils {

    static getEntryPointIndexByCircle(circle: Circle, entryPoints: Array<EntryPoint>): number | null{
        let index = 0;
        for(let entryPoint of entryPoints) {
            if(circle === entryPoint.getCircle()) {
                return index;
            }
            index++;
        }
        return null;
    }

    static getStationIndexByMarker(marker: Marker, stations: Array<Station>): number | null {
        let index = 0;
        for(let station of stations) {
            if(marker === station.getMarker()) {
                return index;
            }
            index++;
        }
        return null;
    }
}

