import {Component, EventEmitter, Input, OnInit, Output, ChangeDetectorRef} from '@angular/core';

@Component({
    selector: 'schema-global-form',
    template: require('./schemaform-global.component.html'),
    styles: []
})
export class SchemaFormGlobalComponent {

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

    ngAfterContentChecked() {
        let schemaformhtml = document.getElementsByTagName('schema-global-form');
        if(schemaformhtml.length !== 0) {
            let submithtml = schemaformhtml.item(0).querySelectorAll('[type="submit"]');
            if(submithtml.length !== 0) {
                submithtml.item(0).setAttribute('style', 'display: none');
            }
        }
        
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