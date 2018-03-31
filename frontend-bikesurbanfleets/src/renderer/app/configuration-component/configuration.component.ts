import {Component, Inject, ViewChild} from "@angular/core";
import {AjaxProtocol} from "../../ajax/AjaxProtocol";
import {Layer, Rectangle, FeatureGroup, Circle, Marker} from "leaflet";
import {SchemaformComponent} from "../schemaform-component/schemaform.component";
import {LeafletDrawFunctions, FormJsonSchema, EntryPoint, Station} from "./config-definitions";
import * as $ from "jquery";
import {ConfigurationLeaflethandler} from "./configuration.leaflethandler";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {EntryPointDataType} from "../../../shared/configuration";
const { dialog } = (window as any).require('electron').remote;


@Component({
    selector: 'configuration',
    template: require('./configuration.component.html'),
    styles: [require('./configuration.component.css')],
})
export class ConfigurationComponent {

    /*
    * Form json schema variables
    */
    globalForm: FormJsonSchema;
    selectEntryPointForm: FormJsonSchema;
    entryPointForm: FormJsonSchema;
    stationForm: FormJsonSchema;

    /*
    * Auxiliar variables
    */
    lastSelectedEntryPointType: any;
    lastStation = {
        position: { latitude: 0, longitude: 0}
    };

    /*
    * Variables for configuration
    */
    globalData = {
        boundingBox: {
            northWest: { latitude: 0, longitude:0 },
            southEast: { latitude: 0, longitude:0 }
        }
    };
    entryPoints: Map<Circle, EntryPoint> = new Map<Circle, EntryPoint>();
    stations: Map<Marker, Station> = new Map<Marker, Station>();

    /*
    * Map control variables
    */
    hasBoundingBox = false;                                 // Check if bounding box drawing
    featureGroup: FeatureGroup = new FeatureGroup();        // List of drawn element
    drawOptions: any;                                       // Draw options for leaflet-draw
    map: L.Map;                                             // showed Map
    lastCircleAdded: Circle;                                // Last circle added
    lastMarkerAdded: Marker;                                // Last Marker added

    //Global Form component
    @ViewChild('globalSchemaForm') gsForm: SchemaformComponent;

    constructor(@Inject('AjaxProtocol') private ajax: AjaxProtocol,
                private modalService: NgbModal) {}

    async ngOnInit() {
        this.drawOptions = LeafletDrawFunctions.createLeafletDrawOptions(this.featureGroup);
        await this.ajax.formSchema.init();
        this.globalFormInit().then();
        this.selectEntryPointFormInit().then();
        this.stationFormInit().then();
    }

    getMapController(map: L.Map) {
        this.map = map;
        this.drawOptions.edit.featureGroup.addTo(this.map);
        this.defineMapEventHandlers();
    }

    defineMapEventHandlers() {
        this.map.on('draw:created', (event: any) => {
            let layer: Layer = event.layer;
            let type = event.layerType;
            switch(type) {
                case 'rectangle': ConfigurationLeaflethandler.onDrawBoundingBoxHandler(<Rectangle> layer, this);
                break;
                case 'circle': ConfigurationLeaflethandler.onDrawEntryPointHandler(<Circle> layer, this);
                break;
                case 'marker': ConfigurationLeaflethandler.onDrawStationHandler(<Marker> layer, this);
            }
        });
        this.map.on('draw:deleted', (event: any) => {
            for (let layer of event.layers.getLayers()) {
                if(layer instanceof Rectangle && this.hasBoundingBox) {
                    ConfigurationLeaflethandler.onDeleteBoundingBoxHandler(this);
                }
                if (layer instanceof Circle) {
                    this.entryPoints.delete(<Circle> layer);
                    this.featureGroup.removeLayer(layer);
                }
                if (layer instanceof Marker) {
                    this.stations.delete(<Marker> layer);
                    this.featureGroup.removeLayer(layer);
                }
            }
        })

    }

    updateGlobalFormView() {
        let tab1 = document.getElementById('added-entities');
        let tab2 = document.getElementById('global-form');
        if(tab1 !== null && tab2 !== null) {
            tab1.click();
            tab2.click();
        }
    }

    globalFormSubmit($event: any) {
        this.globalData = $event;
        dialog.showMessageBox({message: 'Global Configuration Updated', type: 'info'});
    }

    selectEntryPointSubmit(data: EntryPointDataType) {
        $('#form-select-entry-point-close').trigger('click');
        console.log(data);
        this.lastSelectedEntryPointType = data;
        this.entryPointFormInit(data).then((entryPointData: any) => {
            entryPointData.position.latitude = this.lastCircleAdded.getLatLng().lat;
            entryPointData.position.longitude = this.lastCircleAdded.getLatLng().lng;
            if(this.entryPointForm.schema.properties.radius !== null) {
                entryPointData.radius = this.lastCircleAdded.getRadius();
            }
            $('#form-entry-point-button').trigger('click');
        });
    }

    entryPointSubmit(entryPoint: any) {
        entryPoint.entryPointType = {};
        entryPoint.userType = {};
        entryPoint.entryPointType = this.lastSelectedEntryPointType.entryPointType;
        entryPoint.userType.typeName = this.lastSelectedEntryPointType.userType;
        this.lastCircleAdded.setLatLng({
            lat: entryPoint.position.latitude,
            lng: entryPoint.position.longitude
        });
        if(this.lastSelectedEntryPointType.entryPointType === "POISSON") {
            this.lastCircleAdded.setRadius(entryPoint.radius);
        } else {
            this.lastCircleAdded.setRadius(50);
        }
        let newEntryPoint = new EntryPoint(entryPoint, this.lastCircleAdded);
        newEntryPoint.getCircle().bindPopup(newEntryPoint.getPopUp());
        this.entryPoints.set(this.lastCircleAdded, newEntryPoint);
        console.log(this.entryPoints);
        $('#form-entry-point-close').trigger('click');
    }

    stationSubmit(station: any) {
        this.lastMarkerAdded.setLatLng({
            lat: station.position.latitude,
            lng: station.position.longitude
        });
        let newStation = new Station(station, this.lastMarkerAdded);
        this.stations.set(this.lastMarkerAdded, newStation);
        this.lastMarkerAdded.setIcon(newStation.getIcon());
        $('#form-station-close').trigger('click');
    }

    async globalFormInit() {
        this.ajax.formSchema.getGlobalSchema().then((schema) => {
            this.globalForm = {
                schema: schema,
                data: this.globalData
            };
            return;
        });
    }

    async selectEntryPointFormInit(): Promise<void> {
        this.ajax.formSchema.getSchemaFormEntryPointAndUserTypes().then((schema) => {
            this.selectEntryPointForm = {
                schema: schema,
                data: null
            };
            return;
        });
    }

    async entryPointFormInit(selected: EntryPointDataType): Promise<any> {
        let entryPointData = {
            position: {},
            radius: 0
        };
        let schema = await this.ajax.formSchema.getSchemaByTypes(selected);
        console.log(schema);
        this.entryPointForm = {
            schema: schema,
            data: entryPointData
        };
        return entryPointData;
    }

    async stationFormInit(): Promise<void> {
        this.ajax.formSchema.getStationSchema().then((schema) => {
            this.stationForm = {
                schema: schema,
                data: this.lastStation
            };
            return;
        })
    }

    openFormEntryPoint(modalRef: any) {
        this.modalService.open(modalRef, {backdrop: "static", keyboard: false}).result.then((result) => {
            if(result === 'Close click') {
                console.log("Removing circle");
                this.featureGroup.removeLayer(this.lastCircleAdded);
            }
        }, () => {});
    }

    openStationForm(modalRef: any) {
        this.modalService.open(modalRef, {backdrop: "static", keyboard: false}).result.then((result) => {
            if(result === 'Close click') {
                console.log("Removing circle");
                this.featureGroup.removeLayer(this.lastMarkerAdded);
            }
        }, () => {});
    }
}