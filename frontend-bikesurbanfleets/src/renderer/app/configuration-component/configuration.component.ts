import {Component, Inject} from "@angular/core";
import * as $ from "jquery";
import {AjaxProtocol} from "../../ajax/AjaxProtocol";

@Component({
    selector: 'configuration',
    template: require('./configuration.component.html'),
    styles: [require('./configuration.component.css')],
})
export class ConfigurationComponent {

    private globalSchema: any;
    private globalData: any;

    constructor(@Inject('AjaxProtocol') private ajax: AjaxProtocol) {}

    async ngOnInit() {

        this.globalData = {};

        this.globalFormInit().then(() => {
            console.log("global form loaded");
        });
    }

    async globalFormInit() {
        await this.ajax.formSchema.init();
        this.ajax.formSchema.getGlobalSchema().then((schema) => {
            this.globalSchema = schema;
            return;
        });
    }

    globalConfigUpdated(data: any) {
        console.log(data)
        this.globalData = data;
    }

}