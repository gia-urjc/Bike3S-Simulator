import { Component, Inject, OnInit } from '@angular/core';
import { settingsPathGenerator } from '../../shared/settings';
import { AjaxProtocol } from '../ajax/AjaxProtocol';
import {EntryPointDataType} from "../../shared/configuration";

@Component({
    selector: 'my-app',
    template: require('./app.component.html'),
    styles: [require('./app.component.css')]
})
export class AppComponent implements OnInit {

    constructor(@Inject('AjaxProtocol') private ajax: AjaxProtocol) {}

    async ngOnInit() {
        // TEST FORMS
        /*
        this.ajax.formSchema.init().then(() => {
            this.ajax.formSchema.getStationSchema().then((newSchema) => {
                console.log(newSchema);
                this.schema = newSchema;
            })
        })
        */

       /* try {
            let path = settingsPathGenerator();
            console.log(await this.ajax.settings.get(path.layers.mapbox()));
        } catch (error) {
            console.log(error);
        }*/
    }
}
