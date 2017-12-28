import 'bootstrap/dist/css/bootstrap.min.css';
import 'leaflet/dist/leaflet.css';

import { icon, Marker } from 'leaflet';

Marker.prototype.options.icon = icon({
    iconUrl: 'assets/leaflet-default-marker-icon.png',
    shadowUrl: 'assets/leaflet-default-marker-shadow.png',
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    tooltipAnchor: [16, -28],
    shadowSize: [41, 41]
});
