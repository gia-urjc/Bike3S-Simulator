import {Component, Input} from "@angular/core";
const JSONEditor = require('jsoneditor');

@Component({
    selector: 'json-tree-view',
    template: require('./jsoneditor.component.html'),
    styles: [require('./jsoneditor.component.css')]
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
            this.jsonEditor = new JSONEditor(container, {onChange: () => this.onChange()});
            this.jsonEditor.set(this.data);
            console.log(this.data);
        }
    }

    onChange() {
        //TODO Detect changes and send Event to change map figures
    }

}