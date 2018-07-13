import {Component, Input, ChangeDetectorRef} from "@angular/core";
import { EntryPoint } from "../configuration-component/config-definitions";
const JSONEditor = require('jsoneditor');

@Component({
    selector: 'json-tree-view',
    template: require('./jsoneditor.component.html'),
    styles: []
})
export class JsonTreeViewComponent {

    @Input()
    data: any;

    jsonEditor: any;

    constructor() {
    }

    ngOnInit() {
        let container = document.getElementById("json-editor");
        if(container != null) {
            this.jsonEditor = new JSONEditor(container, {readOnly: true});
            this.jsonEditor.set(this.data);
            console.log("JsonTree: " + this.data);
        }
    }

    dataUpdated(data: any) {
        this.data = data;
        this.jsonEditor.set(this.data);
    }

}