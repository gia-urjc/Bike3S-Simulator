import { Component, OnInit, Inject } from "@angular/core";
import { AjaxProtocol } from "../../ajax/AjaxProtocol";

@Component({
    selector: 'configuration-recommendation',
    template: require('./configuration-recommendation.component.html'),
    styles: []
})
export class ConfigurationRecommendationComponent implements OnInit {
    
    constructor(@Inject('AjaxProtocol') private ajax: AjaxProtocol) {

    }

    async ngOnInit(): Promise<void> {
        console.log(await this.ajax.formSchema.getRecommenderTypesSchema());
    }

}