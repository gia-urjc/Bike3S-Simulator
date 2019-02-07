import { Component, OnInit, Inject, ViewChild, Input, Output, EventEmitter, ChangeDetectorRef } from "@angular/core";
import { AjaxProtocol } from "../../ajax/AjaxProtocol";
import { FormJsonSchema, ValidationFormSchemaError } from "../../../shared/ConfigurationInterfaces";
import * as _ from "lodash";
import { SchemaGenericComponent } from "../schemaform-generic-component/schemaform-generic.component";
import { IfStmt } from "@angular/compiler";

@Component({
    selector: 'configuration-recommendation',
    template: require('./configuration-recommendation.component.html'),
    styles: []
})
export class ConfigurationRecommendationComponent implements OnInit {

    selectRecommenderFormSchema: FormJsonSchema;
    recommenderSchema: FormJsonSchema;
    isSelectedRecommenderValid: boolean;
    isRecommenderValid: boolean;
    
    reloading: boolean = false;

    @Input()
    selectedRecommender: any;

    @Input()
    recommenderConfigurationData: any;

    @Output('recommenderSelectedEvent') recommenderSelectedEvent = new EventEmitter<any>();
    @Output('recommenderConfigDataEvent') recommenderConfigDataEvent = new EventEmitter<any>();

    @ViewChild('selectRecommenderForm') selectRecomForm: SchemaGenericComponent;
    @ViewChild('recommenderForm') recommenderForm: SchemaGenericComponent;

    
    constructor(@Inject('AjaxProtocol') private ajax: AjaxProtocol) {

    }

    async ngOnInit(): Promise<void> {
        let schema = await this.ajax.formSchema.getRecommenderTypesSchema();
        this.selectRecommenderFormSchema = {
            schema: schema,
            data: this.selectedRecommender,
            options: {
                addSubmit: false
            }
        };
        if(this.selectedRecommender) {
            this.recommenderSchema = await this.ajax.formSchema.getRecommenderSchemaByType(this.selectedRecommender.recommenderType);
            if(this.recommenderConfigurationData){
                this.recommenderSchema.data = this.recommenderConfigurationData;
            }
            else {
                this.recommenderSchema.data = {};
            }
        }
    }


    async isValidSelectedRecommender(isValid: any): Promise<boolean> {
        if(this.selectRecomForm && !_.isEmpty(this.selectRecomForm.getData())) {
            this.selectedRecommender = this.selectRecomForm.getData();
            this.recommenderSelectedEvent.emit(this.selectedRecommender);
            this.recommenderSchema = await this.ajax.formSchema.getRecommenderSchemaByType(this.selectedRecommender.recommenderType);
            if(this.recommenderConfigurationData){
                this.recommenderSchema.data = this.recommenderConfigurationData;
            }
            else {
                this.recommenderSchema.data = {};
            }
            this.isSelectedRecommenderValid = isValid;
            return isValid;
        }
        this.isSelectedRecommenderValid = false;
        return false;
    }

    isValidRecommender(isValid: any): boolean {
        if(this.recommenderForm && !_.isEmpty(this.recommenderForm.getData())) {
            this.recommenderConfigurationData = this.recommenderForm.getData();
            this.recommenderConfigDataEvent.emit(this.recommenderConfigurationData);
            this.isRecommenderValid = isValid;
            return isValid;
        }
        this.isRecommenderValid = false;
        return false;
    }

    isConfigurationValid(): boolean {
        return this.isRecommenderValid && this.isSelectedRecommenderValid;
    }

    getRecommendationFormErrors(): ValidationFormSchemaError[] {
        return this.recommenderForm.getValidationErrors();
    }

    reload() {
        this.reloading = true;
        setTimeout(() => {
            this.reloading = false;
        }, 0.1);
    }
}