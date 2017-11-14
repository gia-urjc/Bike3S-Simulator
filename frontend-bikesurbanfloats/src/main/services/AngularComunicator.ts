import {ipcMain} from "electron";

module AngularComunicator{

    export enum RequestType{
        Send,
        Receive
    }

    export function request(message:string, method:RequestType, data, funct){
        if(method === RequestType.Receive)
        ipcMain.on(message, (event, arg) => {
            return funct(data);
        });
    }

}