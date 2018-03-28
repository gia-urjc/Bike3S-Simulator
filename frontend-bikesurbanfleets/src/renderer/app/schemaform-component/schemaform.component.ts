import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import * as $ from "jquery";

@Component({
    selector: 'schema-form',
    template: require('./schemaform.component.html'),
    styles: [require('./schemaform.component.css')]
})
export class SchemaformComponent implements OnInit {

    @Input()
    schema: any;

    @Input()
    data: any;

    @Input()
    form: any;

    @Output('dataSubmited')
    dataSubmited = new EventEmitter<any>();

    constructor() {
    }

    ngOnInit(): void {
        console.log(this.form);
    }

    updateData(isValid: boolean) {
        if(isValid) {
            this.dataSubmited.emit(this.data);
        }
    }

}