import * as $ from "jquery";
import {Circle, Layer, Marker, Rectangle} from "leaflet";
import {ConfigurationComponent} from "./configuration.component";

export class ConfigurationLeaflethandler {

    /*
    * ON DRAW METHODS
    */

    static onDrawBoundingBoxHandler(rectangle: Rectangle, component: ConfigurationComponent) {
        if (!component.hasBoundingBox) {
            component.globalData.boundingBox.northWest.latitude = rectangle.getBounds().getNorthWest().lat;
            component.globalData.boundingBox.northWest.longitude = rectangle.getBounds().getNorthWest().lng;
            component.globalData.boundingBox.southEast.latitude = rectangle.getBounds().getSouthEast().lat;
            component.globalData.boundingBox.southEast.longitude = rectangle.getBounds().getSouthEast().lng;
            component.featureGroup.addLayer(rectangle);
            component.hasBoundingBox = true;
            component.globalFormInit().then(() => {
                component.gsForm.resetForm();
                component.updateGlobalFormView();
                $('.leaflet-draw-draw-rectangle').hide();
            });
        } else {
            component.featureGroup.removeLayer(rectangle);
        }
    }

    static onDrawEntryPointHandler(circle: Circle, component: ConfigurationComponent) {
        $('#form-select-entry-point-button').trigger('click');
        component.lastCircleAdded = circle;
        component.featureGroup.addLayer(circle);
    }

    static onDrawStationHandler(marker: Marker, component: ConfigurationComponent) {
        component.lastMarkerAdded = marker;
        component.featureGroup.addLayer(marker);
        component.lastStation.position.latitude = marker.getLatLng().lat;
        component.lastStation.position.longitude = marker.getLatLng().lng;
        $('#form-station-button').trigger('click');
    }

    /*
    * ON DELETE METHODS
    */

    static onDeleteBoundingBoxHandler(component: ConfigurationComponent) {
        $('.leaflet-draw-draw-rectangle').show();
        component.hasBoundingBox = false;
    }

}