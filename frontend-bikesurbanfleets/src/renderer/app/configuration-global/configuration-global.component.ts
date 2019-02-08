import { Component, OnInit, Input, Output, EventEmitter, ViewChild, Inject, AfterViewInit } from "@angular/core";
import { FormJsonSchema, ValidationFormSchemaError } from "../../../shared/ConfigurationInterfaces";
import { SchemaGenericComponent } from "../schemaform-generic-component/schemaform-generic.component";
import { AjaxProtocol } from "../../ajax/AjaxProtocol";
import * as _ from "lodash";

@Component({
    selector: 'configuration-global',
    template: require('./configuration-global.component.html'),
    styles: []
})
export class ConfigurationGlobalComponent implements OnInit {

    globalFormSchema: FormJsonSchema;
    globalFormValid: boolean;
    globalFormErrors: ValidationFormSchemaError[];

    reloading: boolean = false;

    @Input()
    globalConfigData: any;

    @Output('globalConfigDataEvent') globalConfigDataEvent = new EventEmitter<any>(); 

    @ViewChild('globalForm') globalForm: SchemaGenericComponent;

    constructor(@Inject('AjaxProtocol') private ajax: AjaxProtocol) {
    }

    async ngOnInit() {
        this.ajax.jsonLoader.init();
        let globalSchema = JSON.parse(await this.ajax.formSchema.getGlobalSchema());
        let layout = (await this.ajax.jsonLoader.getAllLayouts()).globalLayout;
        this.globalFormSchema = {
            schema: globalSchema,
            data: this.globalConfigData,
            options: {
                addSubmit: false
            },
            layout: layout 
        };
    }

    isValidGlobalForm(isValid: boolean): boolean{
        if(this.globalForm && !_.isEmpty(this.globalForm.getData())) {
            this.globalConfigData = this.globalForm.getData();
            this.globalConfigDataEvent.emit(this.globalConfigData);
            this.globalFormValid = isValid;
            return isValid;
        }
        else {
            this.globalFormValid = false;
            return false;
        }
    }

    isGlobalFormValid(): boolean {
        return this.globalFormValid;
    }

    getGlobalFormErrors(): ValidationFormSchemaError[] {
        return this.globalForm.getValidationErrors();
    }
    
    reload() {
        this.reloading = true;
        setTimeout(() => {
            this.reloading = false;
        }, 0.1);
    }
}
