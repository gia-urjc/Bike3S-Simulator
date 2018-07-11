import { Input, Component, Inject, EventEmitter, Output } from "@angular/core";
import { AjaxProtocol } from "../../ajax/AjaxProtocol";
import { NgbActiveModal } from "@ng-bootstrap/ng-bootstrap";
const  {dialog} = (window as any).require('electron').remote;

@Component({
    selector: 'configuration-load-global',
    template: require('./configuration-load-global.component.html'),
    styles: [require('./configuration-load-global.component.css')]
})
export class ConfigurationLoadGlobalComponent {
    
    @Input()
    path: string;

    globalDataLoaded: any;

    isError: boolean;
    message: string;

    constructor(@Inject('AjaxProtocol') private ajax: AjaxProtocol,
                public activeModal: NgbActiveModal) {
    }

    async ngOnInit() {
        try {
            await this.ajax.jsonLoader.init();
        }
        catch(error) {
            dialog.showErrorBox("Error", `Error loading schemas: ${error}`);
        }
        await this.loadGlobalConfig();
    }

    async ngOnDestroy() {
        this.ajax.jsonLoader.close();
    }


    async loadGlobalConfig() {
        let globalData: any;
        try {
            this.globalDataLoaded = await this.ajax.jsonLoader.loadJson(this.path);
            this.message = "Global configuration loaded";
        }
        catch(error) {
            this.isError = true;
            this.message = error.message; 
        }
    }

    close() {
        this.activeModal.close(this.globalDataLoaded);
    }
}