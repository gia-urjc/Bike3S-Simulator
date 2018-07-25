import * as $ from "jquery";
import {Circle, Layer, Marker, Rectangle} from "leaflet";
import {ConfigurationComponent} from "./configuration.component";
import { EntryPoint, Station } from "./config-definitions";
import { BoundingBox } from "../../../shared/ConfigurationInterfaces";

export class ConfigurationLeaflethandler {

    /*
    * ON DRAW METHODS
    */

    static drawBoundingBox(comp: ConfigurationComponent, rectangle: Rectangle | BoundingBox) {
        //Delete current Rectangle from map
        comp.featureGroup.eachLayer((layer: any) => {
            if(layer instanceof Rectangle) {
                comp.featureGroup.removeLayer(layer);
            }
        });

        // If rectangle is passed, the call comes from the map editor, so the new boundingBox is
        // created based in the rectangle drawn in the map
        if(rectangle instanceof Rectangle) {
            Object.assign(comp.globalData, comp.gsForm.actualData);
            let bBox = comp.globalData.boundingBox;
            bBox.northWest.latitude = rectangle.getBounds().getNorthWest().lat;
            bBox.northWest.longitude = rectangle.getBounds().getNorthWest().lng;
            bBox.southEast.latitude = rectangle.getBounds().getSouthEast().lat;
            bBox.southEast.longitude = rectangle.getBounds().getSouthEast().lng;
            comp.featureGroup.addLayer(rectangle);
            comp.hasBoundingBox = true;
            comp.gsForm.resetForm();
        }

        //If bbox is passed (not the rectangle figure), the call comes after loading a global configuration
        // file, so the new bounding box is based in the data
        else {
            let northWestLat = rectangle.northWest.latitude;
            let northWestLng = rectangle.northWest.longitude;
            let southEastLat = rectangle.southEast.latitude;
            let southEastLon = rectangle.southEast.longitude;
            let newRectangleBbox = new Rectangle([[northWestLat, northWestLng], [southEastLat, southEastLon]]);
            comp.featureGroup.addLayer(newRectangleBbox);
            comp.gsForm.resetForm();
        }
    }

    static drawEntryPoint(comp: ConfigurationComponent, cir: Circle | any) {
        if(cir instanceof Circle) {
            $('#form-select-entry-point-button').trigger('click');
            comp.lastCircleAdded = cir;
            comp.featureGroup.addLayer(cir);
        }
        else {
            let entryPoint = cir;
            let latitude: number = entryPoint.position.latitude;
            let longitude: number = entryPoint.position.longitude;
            let radius: number = entryPoint.radius;
            let circle: Circle = new Circle([latitude, longitude], {radius: radius, color: "#e81b1b"});
            let newEntryPoint = new EntryPoint(entryPoint, circle);
            newEntryPoint.getCircle().bindPopup(newEntryPoint.getPopUp());
            comp.entryPoints.push(newEntryPoint);
            comp.finalEntryPoints.entryPoints.push(newEntryPoint.getEntryPointInfo());
            comp.featureGroup.addLayer(circle);
        }
    }

    static drawStation(comp: ConfigurationComponent, marker: Marker | any) {
        if(marker instanceof Marker) {
            comp.lastMarkerAdded = marker;
            comp.featureGroup.addLayer(marker);
            let pos = comp.lastStation.position;
            pos.latitude = marker.getLatLng().lat;
            pos.longitude = marker.getLatLng().lng;
            $('#form-station-button').trigger('click');
        }
        else {
            let station = marker;
            let latitude = station.position.latitude;
            let longitude = station.position.longitude;
            let newMarker = new Marker([latitude, longitude], {icon: Station.startIcon()});
            let newStation: Station = new Station(station, newMarker); 
            comp.stations.push(newStation);
            comp.finalStations.stations.push(newStation.getStationInfo());
            comp.featureGroup.addLayer(newMarker);
            newMarker.setIcon(newStation.getIcon());
        }
    }

    /*
    * ON DELETE METHODS
    */

    static deleteBoundingBox(comp: ConfigurationComponent) {
        Object.assign(comp.globalData, comp.gsForm.actualData);
        let bBox: BoundingBox = comp.globalData.boundingBox;
        bBox.northWest.latitude = 0;
        bBox.northWest.longitude = 0;
        bBox.southEast.latitude = 0;
        bBox.southEast.longitude = 0;
        comp.gsForm.resetForm();
        comp.hasBoundingBox = false;
    }
}