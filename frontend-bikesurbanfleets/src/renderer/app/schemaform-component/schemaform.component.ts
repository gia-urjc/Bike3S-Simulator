import {Component, EventEmitter, Input, OnInit, Output, ChangeDetectorRef} from '@angular/core';

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

    reloadingForm: boolean;

    actualData: any;

    constructor(private cdRef: ChangeDetectorRef) {
    }

    ngOnInit(): void {
    }

    resetForm() {
        this.reloadingForm = true;
        this.cdRef.detectChanges();
        let jsonForm = document.getElementById("json-form");
        this.reloadingForm = false;
        if(jsonForm) {
            jsonForm.click();
        }
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