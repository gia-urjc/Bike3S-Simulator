import {Component, Input} from '@angular/core';

@Component({
    selector: 'schema-form',
    template: require('./schemaform.component.html'),
    styles: [require('./schemaform.component.css')]
})
export class SchemaformComponent {

    @Input()
    schema: any;

    data: any;

    constructor() {
    }

    submitForm(event: Event) {
        console.log(this.data);
    }

}