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

    ngOnInit() {
    }
}
