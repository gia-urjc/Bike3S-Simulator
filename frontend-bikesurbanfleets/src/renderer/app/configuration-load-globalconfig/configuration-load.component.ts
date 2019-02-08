import { Input, Component, Inject, EventEmitter, Output } from "@angular/core";
import { AjaxProtocol } from "../../ajax/AjaxProtocol";
import { NgbActiveModal } from "@ng-bootstrap/ng-bootstrap";
import { ConfigurationFile } from "../../../shared/ConfigurationInterfaces";
const  {dialog} = (window as any).require('electron').remote;

@Component({
    selector: 'configuration-load',
    template: require('./configuration-load.component.html'),
    styles: []
})
export class ConfigurationLoadComponent {
    
    @Input()
    path: string;

    @Input()
    configurationFile: ConfigurationFile;

    dataLoaded: any;

    isError: boolean;
    message: string;

    constructor(@Inject('AjaxProtocol') private ajax: AjaxProtocol,
                public activeModal: NgbActiveModal) {
    }

    async ngOnInit() {
        this.ajax.jsonLoader.init();
        switch(this.configurationFile) {
            case ConfigurationFile.GLOBAL_CONFIGURATION: await this.loadGlobalConfig(); break;
            case ConfigurationFile.ENTRYPOINT_CONFIGURATION: await this.loadEntryPointConfig(); break;
            case ConfigurationFile.STATION_CONFIGURATION: await this.loadStationsConfig(); break;
        }
    }

    async ngOnDestroy() {
        this.ajax.jsonLoader.close();
    }


    async loadGlobalConfig() {
        try {
            if(this.path) {
                this.dataLoaded = await this.ajax.jsonLoader.loadJsonGlobal(this.path);
                this.message = "Global configuration loaded";
            }
            
        }
        catch(error) {
            this.isError = true;
            this.message = error.message; 
        }
    }

    async loadEntryPointConfig() {
        let entryPointData: any;
        try {
            if(this.path) {
                this.dataLoaded = await this.ajax.jsonLoader.loadJsonEntryPoints(this.path);
                this.message = "Entry Points configuration loaded";
            }
        }
        catch(error) {
            this.isError = true;
            this.message = error.message + "\n";
        }
    }

    async loadStationsConfig() {
        let stations: any;
        try {
            if(this.path) {
                this.dataLoaded = await this.ajax.jsonLoader.loadJsonStations(this.path);
                this.message = "Stations configuration loaded";
            }
        }
        catch(error) {
            this.isError = true;
            this.message = error.message + "\n";
        }
    }

    close() {
        this.activeModal.close(this.dataLoaded);
    }
}