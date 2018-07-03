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

    @Output('automaticSubmit')
    automaticSubmit = new EventEmitter<any>();

    @Output('isValid')
    isValid = new EventEmitter<any>();

    actualData: any;

    constructor() {
    }

    ngOnInit(): void {
    }

    resetForm() {
        console.log(this.form);
    }

    submit(data: any) {
        console.log(data);
        this.dataSubmited.emit(data);
    }

    valid(isValid: any) {
        this.isValid.emit(isValid);
        if(isValid) {
            this.automaticSubmit.emit(this.actualData);
        }
    }

    changed(data: any) {
        this.actualData = data;
        console.log(data);
    }

}