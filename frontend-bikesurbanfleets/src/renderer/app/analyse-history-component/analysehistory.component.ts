import {Component, Inject} from "@angular/core";
import { AjaxProtocol } from "../../ajax/AjaxProtocol";
const { dialog } = (window as any).require('electron').remote;
const { ipcRenderer } = (window as any).require('electron');

@Component({
    selector: 'analyse-history',
    template: require('./analysehistory.component.html'),
    styles: [require('./analysehistory.component.css')]
})
export class AnalyseHistoryComponent {

    historyPath: string;
    csvPath: string;

    constructor(@Inject('AjaxProtocol') private ajax: AjaxProtocol) {}

    async ngOnInit(): Promise<void> {
        try {
            await this.ajax.csvGenerator.init();
            return;
        }
        catch(error) {
            dialog.showErrorBox('Error analysing data', 'Error initializing Analysis Ajax Protocol \n' + error);
        }
    }

    selectFolder() {
        return dialog.showOpenDialog({properties: ['openDirectory', 'createDirectory']})[0];
    }

    async generateData(): Promise<void> {
        console.log(this.historyPath);
        console.log(this.csvPath);
        if(this.historyPath && this.csvPath) {
            try {
                await this.ajax.csvGenerator.writeCsv({historyPath: this.historyPath, csvPath: this.csvPath});
                dialog.showMessageBox({
                    type: 'info',
                    title: 'CSV files with data generated',
                    message: 'CSV files with data analysed has been generated in: \n' 
                        + this.csvPath
                });
            }
            catch(error) {
                dialog.showErrorBox('Error analysing data', 'Error analysing and writing csv files \n' + error.message);
            }
        }
        else {
            dialog.showErrorBox('Input Error', 'All fields are required');
        }
    }

}