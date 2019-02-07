import {Component, Inject, Input} from "@angular/core";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {AjaxProtocol} from "../../ajax/AjaxProtocol";
import { ConfigurationFile, JsonFileInfo } from "../../../shared/ConfigurationInterfaces";
const  {dialog} = (window as any).require('electron').remote;


@Component({
    selector: 'configuration-save',
    template: require('./configurationsave.component.html')
})
export class ConfigurationSaveComponent {

    @Input()
    path: string;

    @Input()
    configurationFile: ConfigurationFile;

    @Input()
    data: string;

    @Input()
    message: string;
    
    @Input()
    isError: boolean = false;

    constructor(@Inject('AjaxProtocol') private ajax: AjaxProtocol,
                public activeModal: NgbActiveModal) {
    }

    async ngOnInit() {
        if(!this.isError) {
            this.ajax.jsonLoader.init();
            this.saveConfig();  
        } 
    }

    async saveConfig() {
        console.log(this.path);
        console.log(this.configurationFile);
        console.log(this.data);
        let jsonInfo: JsonFileInfo = {
            path: this.path,
            data: this.data
        };
        try {
            switch(this.configurationFile) {
                case ConfigurationFile.GLOBAL_CONFIGURATION:
                    await this.ajax.jsonLoader.saveJsonGlobal(jsonInfo);
                    break;
                case ConfigurationFile.ENTRYPOINT_CONFIGURATION:
                    await this.ajax.jsonLoader.saveJsonEntryPoints(jsonInfo);
                    break;
                case ConfigurationFile.STATION_CONFIGURATION: 
                    await this.ajax.jsonLoader.saveJsonStations(jsonInfo);
                    break;
            }
            this.isError = false;
            this.message = `Configuration generated in ${this.path}`;
        }
        catch(e) {
            this.isError = true;
            console.log(e);
            this.message = e.message;
        }
    }
}