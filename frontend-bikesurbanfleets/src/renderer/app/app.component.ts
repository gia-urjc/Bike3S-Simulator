import { Component, Inject, OnInit } from '@angular/core';
import { settingsPathGenerator } from '../../shared/settings';
import { AjaxProtocol } from '../ajax/AjaxProtocol';
import {EntryPointDataType} from "../../shared/ConfigurationInterfaces";

@Component({
    selector: 'my-app',
    template: require('./app.component.html'),
    styles: [require('./app.component.css')]
})
export class AppComponent implements OnInit {

    constructor(@Inject('AjaxProtocol') private ajax: AjaxProtocol) {}

    async ngOnInit() {

    }
}
