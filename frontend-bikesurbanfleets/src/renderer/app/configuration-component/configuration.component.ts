import {Component, Inject, ViewChild, TemplateRef} from "@angular/core";
import {AjaxProtocol} from "../../ajax/AjaxProtocol";
import {Layer, Rectangle, FeatureGroup, Circle, Marker} from "leaflet";
import {LeafletDrawFunctions, FormJsonSchema, EntryPoint, Station} from "./config-definitions";
import {ConfigurationLeaflethandler} from "./configuration.leaflethandler";
import {NgbModal, NgbModalRef} from "@ng-bootstrap/ng-bootstrap";
import {EntryPointDataType, GlobalConfiguration, ConfigurationFile} from "../../../shared/ConfigurationInterfaces";
import {ConfigurationUtils} from "./configuration.utils";
import {ConfigurationSaveComponent} from "../configuration-save-component/configurationsave.component";
import { ConfigurationLoadComponent } from "../configuration-load-globalconfig/configuration-load.component";
import { JsonTreeViewComponent } from "../jsoneditor-component/jsoneditor.component";
import { SchemaFormGlobalComponent } from "../schemaform-global-component/schemaform-global.component";
import { ConfDownMapComponent } from "../configuration-download-map/configuration-download-map.component";
import * as L from 'leaflet';
const  {dialog} = (window as any).require('electron').remote;


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
    globalData: GlobalConfiguration = {
        boundingBox: {
            northWest: { latitude: 0, longitude:0 },
            southEast: { latitude: 0, longitude:0 }
        }
    };
    entryPoints: Array<EntryPoint> = new Array<any>();
    stations: Array<Station> = new Array<any>();

    actualModalOpen: NgbModalRef;

    /*
    * Final configurations
    */
    finalEntryPoints = {
        "entryPoints": new Array<any>()
    };
    finalStations = {
        "stations": new Array<any>()
    };

    hasBoundingBox: boolean;

    @ViewChild('globalSchemaForm') gsForm: SchemaFormGlobalComponent;

    @ViewChild('selecEntryPointType') epSelectModalForm: TemplateRef<any>;
    @ViewChild('entryPoint') epModalForm: TemplateRef<any>;
    @ViewChild('stationModal') stationModalForm: TemplateRef<any>;

    @ViewChild('jsonTreeEp') jsonTreeEp: JsonTreeViewComponent;
    @ViewChild('jsonTreeStation') jsonTreeStation: JsonTreeViewComponent;

    /*
    * Map control variables
    */
    featureGroup: FeatureGroup = new FeatureGroup();        // List of drawn element
    drawOptions: any;                                       // Draw options for leaflet-draw
    map: L.Map;                                             // showed Map
    lastCircleAdded: Circle;                                // Last circle added
    lastMarkerAdded: Marker;                                // Last Marker added

    constructor(@Inject('AjaxProtocol') public ajax: AjaxProtocol,
                public modalService: NgbModal) {}

    async ngOnInit() {
        this.hasBoundingBox = false;
        this.drawOptions = LeafletDrawFunctions.createLeafletDrawOptions(this.featureGroup);
        (L as any).drawLocal = LeafletDrawFunctions.createCustomMessages();
        await this.ajax.formSchema.init();
        this.globalFormInit();
        this.selectEntryPointFormInit();
        this.stationFormInit();
        this.ajax.jsonLoader.init();
    }

    async globalFormInit() {
        let schema = await this.ajax.formSchema.getGlobalSchema();
        let layout = await this.ajax.jsonLoader.getAllLayouts();
        this.globalForm = {
            schema: JSON.parse(schema),
            data: this.globalData,
            options: {
                addSubmit: false
            },
            layout: layout.globalLayout
        };
    }

    async selectEntryPointFormInit(): Promise<void> {
        let schema = await this.ajax.formSchema.getSchemaFormEntryPointAndUserTypes();
        this.selectEntryPointForm = {
            schema:schema,
            data: null
        };
    }

    async entryPointFormInit(selected: EntryPointDataType): Promise<any> {
        let entryPointData = {
            position: {},
            radius: 0
        };
        let schema = await this.ajax.formSchema.getSchemaByTypes(selected);
        let layout = await this.ajax.jsonLoader.getAllLayouts();
/*        if(selected.entryPointType === "POISSON"){
            layout.entryPointLayout.splice(0,0,{key: "lambda", placeholder: "lambda in minutes"});
        }else{
            layout.entryPointLayout.splice(0,0,{key: "timeInstant", placeholder: "User instant appear in seconds"});
        }*/
        this.entryPointForm = {
            schema: JSON.parse(schema),
            data: entryPointData,
            //layout: layout.entryPointLayout
        };
        return entryPointData;
    }

    async stationFormInit(): Promise<void> {
        let schema = await this.ajax.formSchema.getStationSchema();
        let layout = await this.ajax.jsonLoader.getAllLayouts();
        this.stationForm = {
            schema: JSON.parse(schema),
            data: this.lastStation,
            layout: layout.stationsLayout
        };
    }

    getMapController(map: L.Map) {
        this.map = map;
        this.drawOptions.edit.featureGroup.addTo(this.map);
        this.defineMapEventHandlers();
    }

    bboxChanged(data: any) {
        Object.assign(this.globalData.boundingBox, data);
    }

    defineMapEventHandlers() {
        this.map.on('draw:created', (event: any) => {
            let layer: Layer = event.layer;
            let type = event.layerType;
            switch(type) {
                case 'rectangle': ConfigurationLeaflethandler.drawBoundingBox(this, <Rectangle> layer);
                break;
                case 'circle': ConfigurationLeaflethandler.drawEntryPoint(this, <Circle> layer);
                break;
                case 'marker': ConfigurationLeaflethandler.drawStation(this, <Marker> layer);
            }
        });
        this.map.on('draw:deleted', (event: any) => {
            for (let layer of event.layers.getLayers()) {
                if(layer instanceof Rectangle) {
                    ConfigurationLeaflethandler.deleteBoundingBox(this);
                }
                if (layer instanceof Circle) {
                    let index = ConfigurationUtils.getEntryPointIndexByCircle(layer, this.entryPoints);
                    if (index !== null) {
                        this.entryPoints.splice(index, 1);
                        this.finalEntryPoints.entryPoints.splice(index, 1);
                        this.jsonTreeEp.dataUpdated(this.finalEntryPoints);
                    }
                    this.featureGroup.removeLayer(layer);
                }
                if (layer instanceof Marker) {
                    let index = ConfigurationUtils.getStationIndexByMarker(layer, this.stations);
                    if(index !== null) {
                        this.stations.splice(index, 1);
                        this.finalStations.stations.splice(index, 1);
                        this.jsonTreeStation.dataUpdated(this.finalStations);
                    }
                    this.featureGroup.removeLayer(layer);
                }
            }
        });
    }

    globalFormSubmit($event: any) {
        this.globalData = $event;
    }

    isGlobalFormValid($event: any) {
        this.globalConfigValid = $event;
    }

    async selectEntryPointSubmit(data: EntryPointDataType) {
        this.actualModalOpen.close();
        this.lastSelectedEntryPointType = data;
        let entryPointData = await this.entryPointFormInit(data);
        entryPointData.position.latitude = this.lastCircleAdded.getLatLng().lat;
        entryPointData.position.longitude = this.lastCircleAdded.getLatLng().lng;
        if(this.entryPointForm.schema.properties.radius !== null) {
            entryPointData.radius = this.lastCircleAdded.getRadius();
        }
        this.actualModalOpen = this.modalService.open(this.epModalForm);
    }

    entryPointSubmit(entryPoint: any) {
        entryPoint.entryPointType = {};
        entryPoint.entryPointType = this.lastSelectedEntryPointType.entryPointType;
        entryPoint.userType.typeName = this.lastSelectedEntryPointType.userType;
        this.lastCircleAdded.setLatLng({
            lat: entryPoint.position.latitude,
            lng: entryPoint.position.longitude
        });
        if(entryPoint.hasOwnProperty('radius')) {
            this.lastCircleAdded.setRadius(entryPoint.radius);
        } else {
            this.lastCircleAdded.setRadius(50);
        }
        let newEntryPoint = new EntryPoint(entryPoint, this.lastCircleAdded);
        newEntryPoint.getCircle().bindPopup(newEntryPoint.getPopUp());
        this.entryPoints.push(newEntryPoint);
        this.finalEntryPoints.entryPoints.push(newEntryPoint.getEntryPointInfo());
        this.actualModalOpen.close();
        if(this.jsonTreeEp) {
            this.jsonTreeEp.dataUpdated(this.finalEntryPoints);   
        }
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
        this.actualModalOpen.close();
        if(this.jsonTreeStation) {
            this.jsonTreeStation.dataUpdated(this.finalStations);
        }
    }
    openForm(modalRef: any) {
        this.actualModalOpen = this.modalService.open(modalRef, {backdrop: "static", keyboard: false});
    }

    closeFormEntryPoint() {
        this.actualModalOpen.close();
        this.featureGroup.removeLayer(this.lastCircleAdded);
    }

    openStationForm(modalRef: any) {
        this.actualModalOpen = this.modalService.open(modalRef, {backdrop: "static", keyboard: false});
        this.featureGroup.removeLayer(this.lastMarkerAdded);
    }

    closeStationForm() {
        this.actualModalOpen.close();
        this.featureGroup.removeLayer(this.lastMarkerAdded);
    }

    generateConfiguration() {
        let path = this.selectFolder();
        if(this.isGlobalFormValid) {
            this.globalData = this.gsForm.actualData;
        }
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

    saveFile(): string {
        return dialog.showSaveDialog({filters: [{name: 'OSM Files', extensions: ['osm']}]});
    }

    selectFile(): string | undefined {
        return dialog.showOpenDialog({
            properties: ['openFile', 'createDirectory'],
            filters: [{name: 'JSON Files', extensions: ['json']}]
        })[0].replace(/\\/g, "/");
    }

    async loadGlobalConfig() {
        let file = this.selectFile();
        let modalRef: NgbModalRef = this.modalService.open(ConfigurationLoadComponent);
        modalRef.componentInstance.path = file;
        modalRef.componentInstance.configurationFile = ConfigurationFile.GLOBAL_CONFIGURATION;
        modalRef.result.then((globalData) => {
            Object.assign(this.globalData, globalData);
            this.gsForm.resetForm();
            ConfigurationLeaflethandler.drawBoundingBox(this, this.globalData.boundingBox);
        });
        this.hasBoundingBox = true;
    }

    async loadEntryPoints() {
        let file = this.selectFile();
        let modalRef: NgbModalRef = this.modalService.open(ConfigurationLoadComponent);
        modalRef.componentInstance.path = file;
        modalRef.componentInstance.configurationFile = ConfigurationFile.ENTRYPOINT_CONFIGURATION;
        modalRef.result.then((entryPointsConfig: any) => {
            for(let entryPoint of entryPointsConfig.entryPoints) {
                ConfigurationLeaflethandler.drawEntryPoint(this, entryPoint);
            }
            if(this.jsonTreeEp) {
                this.jsonTreeEp.dataUpdated(this.finalEntryPoints);
            }
        });
    }

    async loadStations() {
        let file = this.selectFile();
        let modalRef: NgbModalRef = this.modalService.open(ConfigurationLoadComponent);
        modalRef.componentInstance.path = file;
        modalRef.componentInstance.configurationFile = ConfigurationFile.STATION_CONFIGURATION;
        modalRef.result.then((stationsConfig: any) => {
            for(let station of stationsConfig.stations) {
                ConfigurationLeaflethandler.drawStation(this, station);
            }
            if(this.jsonTreeStation) {
                this.jsonTreeStation.dataUpdated(this.finalStations);
            }
        });
    }

    async downloadMap() {
        let path = this.saveFile();
        Object.assign(this.globalData, this.gsForm.actualData.boundingBox);
        if(path) {
            let modalRef: NgbModalRef = this.modalService.open(ConfDownMapComponent, {backdrop: "static", keyboard: false});
            modalRef.componentInstance.path = path;
            modalRef.componentInstance.boundingBox = this.globalData.boundingBox;
        }
    }

}