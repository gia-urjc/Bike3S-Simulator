import {Component, Inject, ViewChild} from "@angular/core";
import {AjaxProtocol} from "../../ajax/AjaxProtocol";
import {Layer, Rectangle, FeatureGroup, Circle, Marker} from "leaflet";
import {LeafletDrawFunctions, FormJsonSchema, EntryPoint, Station} from "./config-definitions";
import {ConfigurationLeaflethandler} from "./configuration.leaflethandler";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {EntryPointDataType} from "../../../shared/configuration";
import {ConfigurationUtils} from "./configuration.utils";
const  {dialog} = (window as any).require('electron').remote;
import {SchemaformComponent} from "../schemaform-component/schemaform.component";
import {ConfigurationSaveComponent} from "../configuration-save-component/configurationsave.component";
import * as $ from "jquery";
import * as L from 'leaflet';
import 'leaflet-draw';

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
    * Aux variables
    */
    lastSelectedEntryPointType: any;
    lastStation = {
        position: { latitude: 0, longitude: 0}
    };
    globalConfigValid: boolean = false;

    /*
    * Variables for configuration
    */
    globalData = {
        boundingBox: {
            northWest: { latitude: 0, longitude:0 },
            southEast: { latitude: 0, longitude:0 }
        }
    };
    entryPoints: Array<EntryPoint> = new Array<any>();
    stations: Array<Station> = new Array<any>();

    /*
    * Final configurations
    */
    finalEntryPoints = {
        "entryPoints": new Array<any>()
    };
    finalStations = {
        "stations": new Array<any>()
    };

    @ViewChild('globalSchemaForm') gsForm: SchemaformComponent;

    /*
    * Map control variables
    */
    hasBoundingBox = false;                                 // Bounding box exist if true
    featureGroup: FeatureGroup = new FeatureGroup();        // List of drawn element
    drawOptions: any;                                       // Draw options for leaflet-draw
    map: L.Map;                                             // showed Map
    lastCircleAdded: Circle;                                // Last circle added
    lastMarkerAdded: Marker;                                // Last Marker added

    constructor(@Inject('AjaxProtocol') private ajax: AjaxProtocol,
                private modalService: NgbModal) {}

    async ngOnInit() {
        this.drawOptions = LeafletDrawFunctions.createLeafletDrawOptions(this.featureGroup);
        (L as any).drawLocal = LeafletDrawFunctions.createCustomMessages();
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
                    let index = ConfigurationUtils.getEntryPointIndexByCircle(layer, this.entryPoints);
                    if (index !== null) {
                        this.entryPoints.splice(index, 1);
                        this.finalEntryPoints.entryPoints.splice(index, 1);
                    }
                    this.featureGroup.removeLayer(layer);
                }
                if (layer instanceof Marker) {
                    let index = ConfigurationUtils.getStationIndexByMarker(layer, this.stations);
                    if(index !== null) {
                        this.stations.splice(index, 1);
                        this.finalStations.stations.splice(index, 1);
                    }
                    this.featureGroup.removeLayer(layer);
                }
            }
        });
    }

    globalFormSubmit($event: any) {
        this.globalData = $event;
        console.log('Global Configuration Updated');
        console.log(this.globalData);
    }

    isGlobalFormValid($event: any) {
        console.log('Is global Form Valid?: ' + $event);
        this.globalConfigValid = $event;
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
        this.entryPoints.push(newEntryPoint);
        this.finalEntryPoints.entryPoints.push(newEntryPoint.getEntryPointInfo());
        console.log(this.entryPoints);
        $('#form-entry-point-close').trigger('click');
    }

    stationSubmit(station: any) {
        this.lastMarkerAdded.setLatLng({
            lat: station.position.latitude,
            lng: station.position.longitude
        });
        let newStation = new Station(station, this.lastMarkerAdded);
        this.stations.push(newStation);
        this.finalStations.stations.push(newStation.getStationInfo());
        this.lastMarkerAdded.setIcon(newStation.getIcon());
        $('#form-station-close').trigger('click');
    }

    async globalFormInit() {
        this.ajax.formSchema.getGlobalSchema().then((data) => {
            console.log(data);
            this.globalForm = {
                schema: JSON.parse(data),
                data: this.globalData,
                options: {
                    addSubmit: false
                }
            };
            console.log(this.globalForm);
            return;
        });
    }

    async selectEntryPointFormInit(): Promise<void> {
        this.ajax.formSchema.getSchemaFormEntryPointAndUserTypes().then((schema) => {
            this.selectEntryPointForm = {
                schema:schema,
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
        this.entryPointForm = {
            schema: JSON.parse(schema),
            data: entryPointData
        };
        return entryPointData;
    }

    async stationFormInit(): Promise<void> {
        this.ajax.formSchema.getStationSchema().then((schema) => {
            this.stationForm = {
                schema: JSON.parse(schema),
                data: this.lastStation
            };
            return;
        });
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

    updateGlobalFormView() {
        let tab1 = document.getElementById('added-entities');
        let tab2 = document.getElementById('global-form');
        console.log("tpm");
        if(tab1 !== null && tab2 !== null) {
            tab1.click();
            tab2.click();
        }
    }

    generateConfiguration() {
        let path = this.selectFolder();
        const modalRef = this.modalService.open(ConfigurationSaveComponent);
        modalRef.componentInstance.path = path;
        modalRef.componentInstance.globalConfiguration = this.globalData;
        modalRef.componentInstance.entryPointConfiguration = this.finalEntryPoints;
        modalRef.componentInstance.stationConfiguration = this.finalStations;
        modalRef.componentInstance.globalConfigValid = this.globalConfigValid;
    }

    selectFolder(): string {
        return dialog.showOpenDialog({properties: ['openDirectory', 'createDirectory']})[0];
    }

}