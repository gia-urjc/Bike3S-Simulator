import { Component, OnInit, Inject, ViewChild, Input, Output, EventEmitter, ChangeDetectorRef } from "@angular/core";
import { AjaxProtocol } from "../../ajax/AjaxProtocol";
import { FormJsonSchema } from "../../../shared/ConfigurationInterfaces";
import { SchemaFormGlobalComponent } from "../schemaform-global-component/schemaform-global.component";

@Component({
    selector: 'configuration-recommendation',
    template: require('./configuration-recommendation.component.html'),
    styles: []
})
export class ConfigurationRecommendationComponent implements OnInit {

    globalForm: FormJsonSchema;
    
    @Input()
    selectedRecommender: any = {};

    @Input()
    recommenderConfigurationData: any = {};

    @Output('recommenderSelectedEvent') recommenderSelectedEvent = new EventEmitter<any>();

    @ViewChild('selectRecommenderForm') selectRecomForm: SchemaFormGlobalComponent;

    
    constructor(@Inject('AjaxProtocol') private ajax: AjaxProtocol) {

    }

    async ngOnInit(): Promise<void> {
        this.globalForm = {
            schema: await this.ajax.formSchema.getRecommenderTypesSchema(),
            data: this.selectedRecommender,
            options: {
                addSubmit: false
            }
        };
    }


    isValidRecommenderData(isValid: any) {
        this.selectedRecommender = this.selectRecomForm.getData();
        this.recommenderSelectedEvent.emit(this.selectedRecommender);
    }
}