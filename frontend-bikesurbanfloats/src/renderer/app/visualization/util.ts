import { LatLng, Polyline, PolylineOptions } from 'leaflet';
import { DragEndEvent, LeafletEvent, LeafletMouseEvent, PopupEvent, TooltipEvent } from 'leaflet';
import { Geo } from '../../../shared/util';

export namespace LeafletUtil {
    export function latLng(point: Geo.Point) {
        return new LatLng(point.latitude, point.longitude);
    }

    export function polyline(route: Geo.Route, options?: PolylineOptions) {
        return new Polyline(route.points.map(latLng), options);
    }

    export interface LayerEvents {
        Map: {
            add: LeafletEvent,
            remove: LeafletEvent,
        },
        Popup: {
            popupopen: PopupEvent,
            popupclose: PopupEvent,
        },
        Tooltip: {
            tooltipopen: TooltipEvent,
            tooltipclose: TooltipEvent,
        }
    }

    export interface InteractiveLayerEvents extends LayerEvents {
        Mouse: {
            click: LeafletMouseEvent,
            dblclick: LeafletMouseEvent,
            mousedown: LeafletMouseEvent,
            mouseover: LeafletMouseEvent,
            mouseout: LeafletMouseEvent,
            contextmenu: LeafletMouseEvent,
        }
    }

    export interface MarkerEvents extends InteractiveLayerEvents {
        Map: LayerEvents['Map'] & {
            move: LeafletEvent,
        },

        Dragging: {
            dragstart: LeafletEvent,
            movestart: LeafletEvent,
            drag: LeafletEvent,
            dragend: DragEndEvent,
            moveend: LeafletEvent,
        }
    }
}

export function breakPoint() {
    try {
        throw new Error();
    } catch (e) {}
}
