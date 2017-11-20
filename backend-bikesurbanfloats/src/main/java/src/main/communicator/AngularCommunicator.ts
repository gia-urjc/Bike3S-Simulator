import {ipcMain, Event} from 'electron';

export class AngularCommunicator {

    public createGetDataService(messageId: string, fn: any) {
        ipcMain.on(messageId.concat('-get'), (event: Event, arg: any) => {
            let finalData = fn(arg);
            event.sender.send(messageId.concat('-reply'), finalData);
        })
    }

}

