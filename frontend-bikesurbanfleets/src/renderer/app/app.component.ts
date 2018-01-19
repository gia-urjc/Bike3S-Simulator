import { Component, Inject, OnInit } from '@angular/core';
import { settingsPathGenerator } from '../../shared/settings';
import { AjaxProtocol } from '../ajax/AjaxProtocol';

@Component({
    selector: 'my-app',
    template: require('./app.component.html'),
    styles: [require('./app.component.css')]
})
export class AppComponent implements OnInit {

    test: number;
    schema: any;

    constructor(@Inject('AjaxProtocol') private ajax: AjaxProtocol) {}

    async ngOnInit() {
        this.schema = {"type": "object",
            "properties": {
                "first_name": { "type": "string" },
                "last_name": { "type": "string" },
                "address": {
                    "type": "object",
                    "properties": {
                        "street_1": { "type": "string" },
                        "street_2": { "type": "string" },
                        "city": { "type": "string" },
                        "state": {
                            "type": "string",
                            "enum": [ "AL", "AK", "AS", "AZ", "AR", "CA", "CO", "CT", "DE",
                                "DC", "FM", "FL", "GA", "GU", "HI", "ID", "IL", "IN", "IA",
                                "KS", "KY", "LA", "ME", "MH", "MD", "MA", "MI", "MN", "MS",
                                "MO", "MT", "NE", "NV", "NH", "NJ", "NM", "NY", "NC", "ND",
                                "MP", "OH", "OK", "OR", "PW", "PA", "PR", "RI", "SC", "SD",
                                "TN", "TX", "UT", "VT", "VI", "VA", "WA", "WV", "WI", "WY" ]
                        },
                        "zip_code": { "type": "string" }
                    }
                },
                "birthday": { "type": "string" },
                "notes": { "type": "string" },
                "phone_numbers": {
                    "type": "array",
                    "items": {
                        "type": "object",
                        "properties": {
                            "type": { "type": "string", "enum": [ "cell", "home", "work" ] },
                            "number": { "type": "string" }
                        },
                        "required": [ "type", "number" ]
                    }
                }
            },
            "required": [ "last_name" ]}
       /* try {
            let path = settingsPathGenerator();
            console.log(await this.ajax.settings.get(path.layers.mapbox()));
        } catch (error) {
            console.log(error);
        }*/
    }
}
