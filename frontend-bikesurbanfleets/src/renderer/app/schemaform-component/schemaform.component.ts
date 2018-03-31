import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';

@Component({
    selector: 'schema-form',
    template: require('./schemaform.component.html'),
    styles: [require('./schemaform.component.css')]
})
export class SchemaformComponent implements OnInit {

    @Input()
    form: any;

    @Output('dataSubmited')
    dataSubmited = new EventEmitter<any>();

    @Output('onDataChange')
    dataChange = new EventEmitter<any>();

    constructor() {
    }

    ngOnInit(): void {
    }

    resetForm() {
        console.log(this.form);
    }

    submit(data: any) {
        this.dataSubmited.emit(data);
    }

    sendData(data: any) {
        
    }

}