import { Component, Inject, OnInit } from '@angular/core';
import { settingsPathGenerator } from '../../shared/settings';
import { AjaxProtocol } from '../ajax/AjaxProtocol';

@Component({
    selector: 'my-app',
    template: require('./app.component.html'),
    styles: [require('./app.component.css')]
})
export class AppComponent implements OnInit {

    test: number;

    constructor(@Inject('AjaxProtocol') private ajax: AjaxProtocol) {}

    async ngOnInit() {
       /* try {
            let path = settingsPathGenerator();
            console.log(await this.ajax.settings.get(path.layers.mapbox()));
        } catch (error) {
            console.log(error);
        }*/
    }
}
