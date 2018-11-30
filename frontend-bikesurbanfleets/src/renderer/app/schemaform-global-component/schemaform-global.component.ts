import {Component, EventEmitter, Input, OnInit, Output, ChangeDetectorRef} from '@angular/core';
import * as $ from "jquery";

@Component({
    selector: 'schema-global-form',
    template: require('./schemaform-global.component.html'),
    styles: [require('./schemaform-global.component.css')]
})
export class SchemaFormGlobalComponent {

    @Input()
    form: any;

    @Output('dataSubmited')
    dataSubmited = new EventEmitter<any>();

    @Output('isValid')
    isValid = new EventEmitter<any>();

    @Output('bboxChange')
    bboxChange = new EventEmitter<any>();

    reloading: boolean;
    globalUpdate: boolean;

    actualData: any;

    constructor(private cdRef: ChangeDetectorRef) {
    }

    resetForm() {
        this.reloading = true;
        this.cdRef.detectChanges();
        let jsonForm = document.getElementById("json-form");
        this.reloading = false;
        if(jsonForm) {
            jsonForm.click();
        }
    }

    submit(data: any) {
        this.dataSubmited.emit(data);
    }

    valid(isValid: any) {
        this.isValid.emit(isValid);
    }

    onChanges(data: any) {
        if(this.validBbox(data.boundingBox)) {
            this.bboxChange.emit(data.boundingBox);
        }
        this.actualData = data;
    //    console.log(this.actualData);
    }

    getData() {
        return this.actualData;
    }

    private validBbox(boundingBox: any): boolean {
        return boundingBox.northWest.latitude !== 0 && boundingBox.northWest.longitude !== 0
            && boundingBox.southEast.latitude !== 0 && boundingBox.southEast.longitude !== 0;
    }
}