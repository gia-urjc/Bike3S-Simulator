import {ipcMain, Event} from 'electron';

export class AngularComunicator {

    test = ['One', 'Two', 'Three'];
    
    public init() {   
        
        //Get
        ipcMain.on('get-data-test', (event: Event, arg:any) => {
            let data = this.test[arg];
            event.sender.send('get-data-reply', data);
        });
    
    }
    
    
}
