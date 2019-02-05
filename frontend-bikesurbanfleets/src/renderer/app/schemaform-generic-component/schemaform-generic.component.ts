import {Component, EventEmitter, Input, OnInit, Output, ChangeDetectorRef} from '@angular/core';
import { ValidationFormSchemaError } from '../../../shared/ConfigurationInterfaces';

@Component({
    selector: 'schema-generic-form',
    template: require('./schemaform-generic.component.html'),
    styles: [require('./schemaform-generic.component.css')]
})
export class SchemaGenericComponent {

    @Input()
    form: any;

    @Output('dataSubmited')
    dataSubmited = new EventEmitter<any>();

    @Output('isValid')
    isValid = new EventEmitter<any>();

    actualData: any;
    errors: ValidationFormSchemaError[];

    constructor(private cdRef: ChangeDetectorRef) {
    }

    getData(): any {
        return this.actualData;
    }

    getValidationErrors(): ValidationFormSchemaError[] {
        return this.errors;
    }

    validationErrorsHandler(data: any){
        this.errors = data;
    }

    submit(data: any) {
        this.dataSubmited.emit(data);
    }

    valid(isValid: any) {
        this.isValid.emit(isValid);
    }

    onChanges(data: any) {
        this.actualData = data;
    }

}