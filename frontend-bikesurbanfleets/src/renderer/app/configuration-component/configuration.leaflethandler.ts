import * as $ from "jquery";
import {Circle, Layer, Marker, Rectangle} from "leaflet";
import {ConfigurationComponent} from "./configuration.component";
import { EntryPoint, Station } from "./config-definitions";
import { BoundingBox, GlobalConfiguration } from "../../../shared/ConfigurationInterfaces";

export class ConfigurationLeaflethandler {

    /*
    * ON DRAW METHODS
    */
    static drawBoundingBox(comp: ConfigurationComponent, rectangle: Rectangle | BoundingBox) {
        //Delete current Rectangle from map
        let newRectangleBbox: Rectangle;
        comp.featureGroup.eachLayer((layer: any) => {
            if(layer instanceof Rectangle) {
                comp.featureGroup.removeLayer(layer);
                comp.globalData.boundingBox = {};
                comp.updateGlobalData(comp.globalData);
            }
        });

        // If rectangle is passed, the call comes from the map editor, so the new boundingBox is
        // created based in the rectangle drawn in the map
        if(rectangle instanceof Rectangle) {
            comp.featureGroup.addLayer(rectangle);
            newRectangleBbox = rectangle;
        }

        //If bbox is passed (not the rectangle figure), the call comes after loading a global configuration
        // file, so the new bounding box is based in the data
        else {
            let northWestLat = rectangle.northWest.latitude;
            let northWestLng = rectangle.northWest.longitude;
            let southEastLat = rectangle.southEast.latitude;
            let southEastLon = rectangle.southEast.longitude;
            newRectangleBbox = new Rectangle([[northWestLat, northWestLng], [southEastLat, southEastLon]]);
            comp.featureGroup.addLayer(newRectangleBbox);
        }
        if(!comp.globalData) {
            comp.globalData = {};
        }
        let newGlobalData: GlobalConfiguration = comp.globalData;
        newGlobalData.boundingBox = {
            northWest: {
                latitude: (<any>newRectangleBbox).getLatLngs()[0][1].lat,
                longitude: (<any>newRectangleBbox).getLatLngs()[0][1].lng
            },
            southEast: {
                latitude: (<any>newRectangleBbox).getLatLngs()[0][3].lat,
                longitude: (<any>newRectangleBbox).getLatLngs()[0][3].lng
            }
        };
        comp.hasBoundingBox = true;
        comp.updateGlobalData(comp.globalData);
        //Press a button to update the render
        (<any>document.getElementById("global-form")).click();
    }

    static drawEntryPoint(comp: ConfigurationComponent, cir: Circle | any) {
        if(cir instanceof Circle) {
            $('#form-select-entry-point-button').trigger('click');
            comp.lastCircleAdded = cir;
            comp.featureGroup.addLayer(cir);
        }
        else {
            let entryPoint = cir;
            let latitude: number = entryPoint.positionAppearance.latitude;
            let longitude: number = entryPoint.positionAppearance.longitude;
            let radiusAppears: number = entryPoint.radiusAppears;
            let circle: Circle = new Circle([latitude, longitude], {radius: radiusAppears, color: "#e81b1b"});
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
        if(!comp.globalData) {
            comp.globalData = {};
        }
        comp.globalData.boundingBox = {};
        comp.updateGlobalData(comp.globalData);
        comp.hasBoundingBox = false;
    }
}