import {Component, EventEmitter, Input, OnInit, Output, ChangeDetectorRef} from '@angular/core';

@Component({
    selector: 'schema-form',
    template: require('./schemaform.component.html'),
    styles: []
})
export class SchemaFormComponent implements OnInit {
    
    @Input()
    form: any;

    @Output('dataSubmited')
    dataSubmited = new EventEmitter<any>();

    @Output('isValid')
    isValid = new EventEmitter<any>();

    reloading: boolean;
    globalUpdate: boolean;

    actualData: any;

    constructor(private cdRef: ChangeDetectorRef) {
    }

    ngOnInit(): void {
        console.log(this.form);
        
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
}