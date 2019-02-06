import {Component, Inject, ViewChild, TemplateRef, NgZone} from "@angular/core";
import {AjaxProtocol} from "../../ajax/AjaxProtocol";
import {Layer, Rectangle, FeatureGroup, Circle, Marker} from "leaflet";
import {LeafletDrawFunctions, EntryPoint, Station} from "./config-definitions";
import {ConfigurationLeaflethandler} from "./configuration.leaflethandler";
import {NgbModal, NgbModalRef} from "@ng-bootstrap/ng-bootstrap";
import {EntryPointDataType, GlobalConfiguration, ConfigurationFile, FormJsonSchema, ValidationFormSchemaError} from "../../../shared/ConfigurationInterfaces";
import {ConfigurationUtils} from "./configuration.utils";
import { ConfigurationLoadComponent } from "../configuration-load-globalconfig/configuration-load.component";
import { JsonTreeViewComponent } from "../jsoneditor-component/jsoneditor.component";
import { ConfDownMapComponent } from "../configuration-download-map/configuration-download-map.component";
import * as L from 'leaflet';
import * as _ from 'lodash';
import { ConfigurationRecommendationComponent } from "../configuration-recommendation/configuration-recommendation.component";
import { ConfigurationGlobalComponent } from "../configuration-global/configuration-global.component";
import { ConfigurationSaveComponent } from "../configuration-save-component/configurationsave.component";
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
    selectEntryPointForm: FormJsonSchema;
    entryPointForm: FormJsonSchema;
    stationForm: FormJsonSchema;

    /*
    * Aux variables
    */
    lastSelectedEntryPointType: any;

    globalConfigValid: boolean = false;
    globalConfigErrors: ValidationFormSchemaError[];
    recommenderValidConfig: boolean = false;
    recommenderConfigErrors: ValidationFormSchemaError[];

    /*
    * Variables for configuration
    */
    globalData: GlobalConfiguration;
    entryPoints: Array<EntryPoint> = new Array<any>();
    stations: Array<Station> = new Array<any>();
    selectedRecommender: any;
    recommenderConfigurationData: any;

    /*
    * modal for pop ups
    */
    actualModalOpen: NgbModalRef;

    /*
    * Final configurations
    */
    finalGlobalConfiguration = {};
    finalEntryPoints = {
        "entryPoints": new Array<any>()
    };
    finalStations = {
        "stations": new Array<any>()
    };

    hasBoundingBox: boolean;

    @ViewChild('globalSchemaForm') gsForm: ConfigurationGlobalComponent;
    @ViewChild('recommendationSchemaForm') rsForm: ConfigurationRecommendationComponent; 

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
    currentCirleEntryPoint: Circle;                                // Last circle added
    currentMarkerStation: Marker;                                // Last Marker added

    constructor(@Inject('AjaxProtocol') public ajax: AjaxProtocol,
                public modalService: NgbModal, private zone:NgZone) {
        
        //this is necessary to bind edit and delete buttons from entities placed in the map
        // to call methods declared in this component
        //It just makes accesible methods to be called from js scripts not related with angular

        (<any>window).angularComponentRef = {
            zone: this.zone, 
            editStation: (station: Station) => this.editStation(station),
            editEntryPoint: (entryPoint: EntryPoint) => this.editEntryPoint(entryPoint), 
            component: this
        };
    }

    async ngOnInit() {
        this.hasBoundingBox = false;
        this.drawOptions = LeafletDrawFunctions.createLeafletDrawOptions(this.featureGroup);
        (L as any).drawLocal = LeafletDrawFunctions.createCustomMessages();
        await this.ajax.formSchema.init();
        this.selectEntryPointFormInit();
        this.stationFormInit();
        this.ajax.jsonLoader.init();
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
            positionAppearance: {},
            radiusAppears: 0
        };
        let schema = await this.ajax.formSchema.getSchemaByTypes(selected);
        this.entryPointForm = {
            schema: JSON.parse(schema),
            data: entryPointData,
        };
        return entryPointData;
    }

    async stationFormInit(): Promise<void> {
        let schema = await this.ajax.formSchema.getStationSchema();
        let layout = await this.ajax.jsonLoader.getAllLayouts();
        this.stationForm = {
            schema: JSON.parse(schema),
            data: { 
                position: {
                    latitude: 0,
                    longitude: 0
                }
            },
            layout: layout.stationsLayout
        };
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
                        if(this.jsonTreeEp) {
                            this.jsonTreeEp.dataUpdated(this.finalEntryPoints);
                        }
                    }
                    this.featureGroup.removeLayer(layer);
                }
                if (layer instanceof Marker) {
                    let index = ConfigurationUtils.getStationIndexByMarker(layer, this.stations);
                    if(index !== null) {
                        this.stations.splice(index, 1);
                        this.finalStations.stations.splice(index, 1);
                        if(this.jsonTreeStation) {
                            this.jsonTreeStation.dataUpdated(this.finalStations);
                        }
                    }
                    this.featureGroup.removeLayer(layer);
                }
            }
        });
    }

    async selectEntryPointSubmit(data: EntryPointDataType) {
        this.actualModalOpen.close();
        this.lastSelectedEntryPointType = data;
        let entryPointData = await this.entryPointFormInit(data);
        entryPointData.positionAppearance.latitude = this.currentCirleEntryPoint.getLatLng().lat;
        entryPointData.positionAppearance.longitude = this.currentCirleEntryPoint.getLatLng().lng;
        if(this.entryPointForm.schema.properties.radiusAppears !== null) {
            entryPointData.radiusAppears = this.currentCirleEntryPoint.getRadius();
        }
        this.actualModalOpen = this.modalService.open(this.epModalForm);
    }

    entryPointSubmit(entryPoint: any) {

        //if entry point is present, the method was called from an entry point edition
        //it's necessary to remove the previous data
        let entryPointIndex: number | null = ConfigurationUtils
            .getEntryPointIndexByCircle(this.currentCirleEntryPoint, this.entryPoints);

        entryPoint.entryPointType = {};
        entryPoint.entryPointType = this.lastSelectedEntryPointType.entryPointType;
        entryPoint.userType.typeName = this.lastSelectedEntryPointType.userType;
        this.currentCirleEntryPoint.setLatLng({
            lat: entryPoint.positionAppearance.latitude,
            lng: entryPoint.positionAppearance.longitude
        });
        if(entryPoint.hasOwnProperty('radiusAppears')) {
            this.currentCirleEntryPoint.setRadius(entryPoint.radiusAppears);
        } else {
            this.currentCirleEntryPoint.setRadius(50);
        }
        let newEntryPoint = new EntryPoint(entryPoint, this.currentCirleEntryPoint);
        newEntryPoint.getCircle().bindPopup(newEntryPoint.getPopUp());
        
        if(entryPointIndex !== null) {
            this.entryPoints[entryPointIndex] = newEntryPoint;
            this.finalEntryPoints.entryPoints[entryPointIndex] = newEntryPoint.getInfo();
        }
        else {
            this.entryPoints.push(newEntryPoint);
            this.finalEntryPoints.entryPoints.push(newEntryPoint.getInfo());
        }
        this.currentCirleEntryPoint.on('click', () => newEntryPoint.updatePopUp());
        this.actualModalOpen.close();
        if(this.jsonTreeEp) {
            this.jsonTreeEp.dataUpdated(this.finalEntryPoints);   
        }
    }

    stationSubmit(station: any) {
        
        //if station is present, the method was called from an station edition
        //it's necessary to delete the previous data
        let stationIndex: number | null = ConfigurationUtils.getStationIndexByMarker(this.currentMarkerStation, this.stations);

        station.position.latitude = this.currentMarkerStation.getLatLng().lat;
        station.position.longitude = this.currentMarkerStation.getLatLng().lng;

        let newStation = new Station(station, this.currentMarkerStation);
        newStation.getMarker().bindPopup(newStation.getPopUp());
        if(stationIndex !== null) {
            this.stations[stationIndex] = newStation;
            this.finalStations.stations[stationIndex] = newStation.getInfo();
        }
        else {
            this.stations.push(newStation);
            this.finalStations.stations.push(newStation.getInfo());
        }
        this.currentMarkerStation.on('click', () => newStation.updatePopUp());
        this.currentMarkerStation.setIcon(newStation.getIcon());
        this.actualModalOpen.close();
        if(this.jsonTreeStation) {
            this.jsonTreeStation.dataUpdated(this.finalStations);
        }

        //restart form
        this.stationFormInit();
    }
    openForm(modalRef: any) {
        this.actualModalOpen = this.modalService.open(modalRef, {backdrop: "static", keyboard: false});
    }

    closeFormEntryPoint() {
        let entryPointIndex: number | null = ConfigurationUtils.getEntryPointIndexByCircle(this.currentCirleEntryPoint, this.entryPoints);
        this.actualModalOpen.close();
        if(entryPointIndex === null) {
            this.featureGroup.removeLayer(this.currentCirleEntryPoint);
        }
    }

    openStationForm(modalRef: any) {
        this.actualModalOpen = this.modalService.open(modalRef, {backdrop: "static", keyboard: false});
        this.featureGroup.removeLayer(this.currentMarkerStation);
    }

    closeStationForm() {
        let stationIndex: number | null = ConfigurationUtils.getStationIndexByMarker(this.currentMarkerStation, this.stations);
        this.actualModalOpen.close();
        if(stationIndex === null) {
            this.featureGroup.removeLayer(this.currentMarkerStation);
        }
    }

    saveGlobalConfig() {
        if(!this.hasBoundingBox) {
            const modalRef = this.modalService.open(ConfigurationSaveComponent);
            modalRef.componentInstance.isError = true;
            modalRef.componentInstance.message = "Bounding Box not defined";
        }
        else if (this.globalConfigValid && this.recommenderValidConfig) {
            let finalGlobalConfig: GlobalConfiguration;
            finalGlobalConfig = this.globalData;
            finalGlobalConfig.recommendationSystemType = {};
            finalGlobalConfig.recommendationSystemType.typeName = this.selectedRecommender.recommenderType;
            finalGlobalConfig.recommendationSystemType.parameters = this.recommenderConfigurationData;
            let path = this.saveJsonFile();
            if(path) {
                const modalRef = this.modalService.open(ConfigurationSaveComponent);
                modalRef.componentInstance.configurationFile = ConfigurationFile.GLOBAL_CONFIGURATION;
                modalRef.componentInstance.path = path;
                modalRef.componentInstance.data = finalGlobalConfig;
            }
        }
    }

    saveEntryPointsConfig() {
        if(this.finalEntryPoints.entryPoints.length === 0) {
            const modalRef = this.modalService.open(ConfigurationSaveComponent);
            modalRef.componentInstance.isError = true;
            modalRef.componentInstance.message = "No entry points were added";
        }
        else {
            let path = this.saveJsonFile();
            const modalRef = this.modalService.open(ConfigurationSaveComponent);
            modalRef.componentInstance.configurationFile = ConfigurationFile.ENTRYPOINT_CONFIGURATION;
            modalRef.componentInstance.path = path;
            modalRef.componentInstance.data = this.finalEntryPoints; 
        }
    }

    saveStationsConfig() {
        if(this.finalStations.stations.length === 0) {
            const modalRef = this.modalService.open(ConfigurationSaveComponent);
            modalRef.componentInstance.isError = true;
            modalRef.componentInstance.data = "No stations were added";
        }
        else {
            let path = this.saveJsonFile();
            const modalRef = this.modalService.open(ConfigurationSaveComponent);
            modalRef.componentInstance.configurationFile = ConfigurationFile.STATION_CONFIGURATION;
            modalRef.componentInstance.path = path;
            modalRef.componentInstance.data = this.finalStations;
        }
    }

    selectFolder(): string {
        return dialog.showOpenDialog({properties: ['openDirectory', 'createDirectory']})[0];
    }

    saveOSMFile(): string {
        return dialog.showSaveDialog({
            filters: [{name: 'OSM Files', extensions: ['osm']}],
            properties: ['createDirectory']
        });
    }

    saveJsonFile(): string {
        return dialog.showSaveDialog({
            filters: [{name: 'JSON Files', extensions: ['json']}],
            properties: ['createDirectory']
        });
    }

    selectFile(): string | undefined {
        return dialog.showOpenDialog({
            properties: ['openFile', 'createDirectory'],
            filters: [{name: 'JSON Files', extensions: ['json']}]
        })[0].replace(/\\/g, "/");
    }

    recommenderSelectedHandler(selectedRecommender: any) {
        this.selectedRecommender = selectedRecommender;
    }

    recommenderDataHandler(recommenderData: any) {
        this.recommenderConfigurationData = recommenderData;
        this.recommenderValidConfig = this.rsForm.isConfigurationValid();
        this.recommenderConfigErrors = this.rsForm.getRecommendationFormErrors();
        console.log(this.recommenderValidConfig);
        console.log(this.recommenderConfigErrors);
    }

    globalConfigHandler(globalData: any) {
        if(this.globalData && (this.globalData.boundingBox || _.isEmpty(this.globalData.boundingBox))) {
            let boundingBox = this.globalData.boundingBox;
            this.globalData = globalData;   
            this.globalData.boundingBox = boundingBox;
        }
        else {
            this.globalData = globalData;
        }
        this.globalConfigErrors = this.gsForm.getGlobalFormErrors();
        this.globalConfigValid = this.gsForm.isGlobalFormValid();
        console.log(this.globalConfigValid);
        console.log(this.gsForm.getGlobalFormErrors());
    }

    async loadGlobalConfig() {
        let file = this.selectFile();
        let modalRef: NgbModalRef = this.modalService.open(ConfigurationLoadComponent);
        modalRef.componentInstance.path = file;
        modalRef.componentInstance.configurationFile = ConfigurationFile.GLOBAL_CONFIGURATION;
        modalRef.result.then((globalData) => {
            let recommendationSystemLoaded = globalData.recommendationSystemType;
            delete globalData.recommendationSystemType;
            this.globalData = globalData;
            this.selectedRecommender = {};
            this.selectedRecommender.recommenderType = recommendationSystemLoaded.typeName;
            this.recommenderConfigurationData = {};
            this.recommenderConfigurationData.parameters = recommendationSystemLoaded.parameters;
            this.updateGlobalData(this.globalData);
            ConfigurationLeaflethandler.drawBoundingBox(this, this.globalData.boundingBox);
            if(this.gsForm) {
                this.gsForm.globalFormSchema.data = this.globalData;
                this.gsForm.reload();
            }
            else if(this.rsForm) {
                this.rsForm.selectRecommenderFormSchema.data = this.selectedRecommender;
                this.rsForm.reload();
            }
            (<any>document.getElementById("global-form")).click();
        });
        this.hasBoundingBox = true;
    }

    async loadEntryPoints() {
        let file = this.selectFile();
        let modalRef: NgbModalRef = this.modalService.open(ConfigurationLoadComponent);
        modalRef.componentInstance.path = file;
        modalRef.componentInstance.configurationFile = ConfigurationFile.ENTRYPOINT_CONFIGURATION;
        modalRef.result.then((entryPointsConfig: any) => {
            console.log(entryPointsConfig);
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

    editStation(station: Station) {
        this.stationForm.data = station.getInfo();
        this.currentMarkerStation = station.getMarker();
        this.actualModalOpen = this.modalService.open(this.stationModalForm, {backdrop: "static", keyboard: false});
    }

    editEntryPoint(entryPoint: EntryPoint) {
        this.entryPointForm.data = entryPoint.getInfo();
        this.currentCirleEntryPoint = entryPoint.getCircle();
        this.actualModalOpen = this.modalService.open(this.epModalForm, {backdrop: "static", keyboard: false});
    }

    async downloadMap() {
        let path = this.saveOSMFile();
        if(path) {
            let modalRef: NgbModalRef = this.modalService.open(ConfDownMapComponent, {backdrop: "static", keyboard: false});
            modalRef.componentInstance.path = path;
            modalRef.componentInstance.boundingBox = this.globalData.boundingBox;
        }
    }

    updateGlobalData(globalData: GlobalConfiguration) {
        if(!this.globalData) {
            this.globalData = {};
        }
        let boundingBox = this.globalData.boundingBox;
        if(globalData) {
            this.globalData = globalData;
        }
        this.globalData.boundingBox = boundingBox;
        if(this.gsForm) {
            this.gsForm.globalConfigData = this.globalData;
            console.log(this.gsForm.globalConfigData);
        }
    }

}