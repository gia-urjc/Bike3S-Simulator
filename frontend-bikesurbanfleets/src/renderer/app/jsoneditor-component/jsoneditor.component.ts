import {Component, Input, ChangeDetectorRef} from "@angular/core";
import { EntryPoint, Station } from "../configuration-component/config-definitions";
import { ConfigurationEntity } from "../configuration-component/config-definitions/ConfigDefinitions";
const JSONEditor = require('jsoneditor');

@Component({
    selector: 'json-tree-view',
    template: require('./jsoneditor.component.html'),
    styles: []
})
export class JsonTreeViewComponent {

    @Input()
    finalConfiguration: any;

    @Input()
    arrayOfEntities: Array<Station> | Array<EntryPoint>;

    jsonEditor: any;

    constructor() {
    }

    ngOnInit() {
        let container = document.getElementById("json-editor");
        let options = {
            mode: 'view',
            onEvent: (node: JsonEditorNode, event: Event) => {
                if (event.type === 'click') {
                    this.triggerEntityOnMap(node.path);  
                }
            }
        };
        if(container != null) {
            this.jsonEditor = new JSONEditor(container, options);
            this.jsonEditor.set(this.finalConfiguration);
        //    console.log("JsonTree: " + this.data);
        }
    }

    dataUpdated(data: any) {
        this.finalConfiguration = data;
        this.jsonEditor.set(this.finalConfiguration);
    }

    triggerEntityOnMap(path: Array<any>) {
        let typeEntityClicked: string;
        let indexOfEntity: number;
        if(path.length >= 2 && (path[0] === "stations"|| path[0] === "entryPoints" )) {
            typeEntityClicked = path[0];
            indexOfEntity = path[1];
            console.log(typeEntityClicked);
            console.log(indexOfEntity);
            let entity: ConfigurationEntity;
            if(Number.isInteger(indexOfEntity)) {
                entity = this.arrayOfEntities[indexOfEntity];
                entity.openPopUp();
            } 
        }
    }

}

interface JsonEditorNode {
    field: string;
    value: any;
    path: Array<any>;
}