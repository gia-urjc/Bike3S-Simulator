import { Input, Component, Inject, EventEmitter, Output } from "@angular/core";
import { AjaxProtocol } from "../../ajax/AjaxProtocol";
import { NgbActiveModal } from "@ng-bootstrap/ng-bootstrap";

@Component({
    selector: 'configuration-load-global',
    template: require('./configuration-load-global.component.html'),
    styles: [require('./configuration-load-global.component.css')]
})
export class ConfigurationLoadGlobalComponent {
    
    @Input()
    path: string;

    @Output('globalDataLoaded')
    globalDataLoaded = new EventEmitter<any>();

    constructor(@Inject('AjaxProtocol') private ajax: AjaxProtocol,
                public activeModal: NgbActiveModal) {
    }

    public async loadGlobalConfig() {
        let globalData: any = await this.ajax.jsonLoader.loadJson(this.path);
        this.globalDataLoaded.emit(globalData);
    }
}