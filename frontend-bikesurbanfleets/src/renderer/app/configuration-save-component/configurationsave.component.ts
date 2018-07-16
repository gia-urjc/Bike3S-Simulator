import {Component, Inject, Input} from "@angular/core";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {AjaxProtocol} from "../../ajax/AjaxProtocol";
const  {dialog} = (window as any).require('electron').remote;


@Component({
    selector: 'configuration-save',
    template: require('./configurationsave.component.html'),
    styles: [require('./configurationsave.component.css')]
})
export class ConfigurationSaveComponent {

    @Input()
    path: string;

    @Input()
    globalConfigValid: boolean;

    @Input()
    globalConfiguration: any;

    @Input()
    entryPointConfiguration: any;

    @Input()
    stationConfiguration: any;

    message: string;

    isError: boolean = false;

    constructor(@Inject('AjaxProtocol') private ajax: AjaxProtocol,
                public activeModal: NgbActiveModal) {
    }

    async ngOnInit() {
        try {
            await this.ajax.jsonLoader.init();
        }
        catch(e) {
            dialog.showErrorBox("Error", `Error loading schemas: ${e}`);
        }
        await this.generateConfiguration();
    }

    async ngOnDestroy() {
        this.ajax.jsonLoader.close();
    }

    async generateConfiguration() {
        if(!this.globalConfigValid) {
            this.isError = true;
            this.message = "Global Configuration is not valid";
        }
        else {
            try {
                await this.ajax.jsonLoader.writeJson({
                    json: this.globalConfiguration, path: this.path + "/global-configuration.json"});
                await this.ajax.jsonLoader.writeJson({
                    json: this.entryPointConfiguration, path: this.path  + "/entry-points-configuration.json"});
                await this.ajax.jsonLoader.writeJson({
                    json: this.stationConfiguration, path: this.path + "/stations-configuration.json"});
                this.message = `Configuration generated in ${this.path}`;
            }
            catch(e) {
                this.message = "An error has ocurred";
                console.log(e);
            }
        }
    }

    selectFolder(): string {
        return dialog.showOpenDialog({properties: ['openDirectory']})[0];
    }
}