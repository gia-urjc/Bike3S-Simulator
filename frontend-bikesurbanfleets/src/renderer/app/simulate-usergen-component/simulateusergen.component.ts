import {Component, Inject} from "@angular/core";
import {UserGeneratorArgs} from "../../../shared/BackendInterfaces";
import {AjaxProtocol} from "../../ajax/AjaxProtocol";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
const { dialog } = (window as any).require('electron').remote;
const { ipcRenderer } = (window as any).require('electron');
import * as $ from "jquery";

@Component({
    selector: 'simulateusergen-component',
    template: require('./simulateusergen.component.html'),
    styles: [require('./simulateusergen.component.css')]
})
export class SimulateusergenComponent {

    private globalConfiguration: string;
    private entryPointConfiguration: string;

    private usersOutputFolder: string;

    private resultMessage: string;
    private exceptions: string;
    private stdout: string;
    private errors: boolean;

    constructor(@Inject('AjaxProtocol') private ajax: AjaxProtocol, private modalService: NgbModal) {}

    async ngOnInit() {
        this.resultMessage = "";
        this.exceptions = "";

        ipcRenderer.on('user-gen-error' , (event: Event, data: string) => this.addErrors(data));
        ipcRenderer.on('user-gen-data', (event: Event, data: string) => this.addConsoleMessage(data));

        await this.ajax.backend.init();
    }

    selectFile(): string {
        return dialog.showOpenDialog({
            properties: ['openFile', 'createDirectory'],
            filters: [{name: 'JSON Files', extensions: ['json']}]
        })[0];
    }

    selectFolder(): string {
        return dialog.showOpenDialog({properties: ['openDirectory', 'createDirectory']})[0];
    }

    open(content: any) {
        this.modalService.open(content).result.then(() => {
        });
    }

    addErrors(error: string) {
        this.errors = true;
        this.resultMessage = "Users generation has ended with exceptions";
        this.exceptions += error;
    }

    addConsoleMessage(message: string) {
        console.log(message);
        this.stdout += message;
    }

    async generateUsers() {
        this.errors = false;
        this.resultMessage = "Generating...";
        this.exceptions = "";
        this.stdout = "";
        let args: UserGeneratorArgs = {
            globalConfPath: this.globalConfiguration,
            entryPointsConfPath: this.entryPointConfiguration,
            outputUsersPath: this.usersOutputFolder
        };
        $('#modal-button').trigger('click');
        await this.ajax.backend.generateUsers(args);
        if(!this.errors) {
            this.resultMessage = "Users generated";
        }
    }

}
