import {Component, Inject} from "@angular/core";
import {AjaxProtocol} from "../../ajax/AjaxProtocol";
import {UserGeneratorArgs} from "../../../shared/BackendInterfaces";
const { dialog } = (window as any).require('electron').remote;
const { ipcRenderer } = (window as any).require('electron');

@Component({
    selector: 'simulate-component',
    template: require('./simulate.component.html'),
    styles: [require('./simulate.component.css')]
})
export class SimulateComponent {

    private globalConfiguration: string;
    private stationConfiguration: string;
    private usersConfiguration: string;
    private entryPointConfiguration: string;

    private usersOutputFolder: string;
    private historyOutputFolder: string;

    constructor(@Inject('AjaxProtocol') private ajax: AjaxProtocol) {}

    async ngOnInit() {
        ipcRenderer.on('error' , (event: any , data: any) => { console.log(data)});
        await this.ajax.backend.init();
        console.log("Component started");
    }

    selectFile(): string {
        return dialog.showOpenDialog({
            properties: ['openFile'],
            filters: [{name: 'JSON Files', extensions: ['json']}]
        })[0];
    }

    selectFolder(): string {
        return dialog.showOpenDialog({properties: ['openDirectory']})[0];
    }

    async generateUsers() {
        let args: UserGeneratorArgs = {
            globalConf: this.globalConfiguration,
            entryPointsConf: this.entryPointConfiguration,
            outputUsers: this.usersOutputFolder
        };
        await this.ajax.backend.generateUsers(args);
        console.log("Created");
    }


}