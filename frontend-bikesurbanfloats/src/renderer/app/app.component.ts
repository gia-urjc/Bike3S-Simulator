import { Component, Inject, OnInit } from '@angular/core';
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
        try {
            await this.ajax.history.init('history');
            console.log(await this.ajax.history.entities());
        } catch (error) {
            console.log(error);
        }
    }
}
