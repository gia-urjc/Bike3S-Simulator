import {ipcMain, Event} from 'electron';

module AngularComunicator {


    export function receiveToRenderer(syncMessage: string, data: any, funct: Function) {
        ipcMain.on(syncMessage, (event: Event) => {
            return funct(data);
        })
    }

    export function sendToRenderer(asyncMessage: string, asyncReply: string, data: any) {
        ipcMain.on(asyncMessage, (event: Event) => {
            event.sender.send(asyncReply, data);
        })
    }
}
