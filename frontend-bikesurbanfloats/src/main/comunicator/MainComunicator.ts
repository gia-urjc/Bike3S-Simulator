import {ipcMain, Event} from 'electron';
import {Observable} from 'rxjs';
import { Observer } from 'rxjs/Observer';

export function Receive(asyncMessage: string) {

    return (target: any, propertyKey: string, descriptor: PropertyDescriptor): any => {
        let oldValue = descriptor.value;
        let observable = Observable.create((observer: Observer<any>) => {
            ipcMain.on(asyncMessage, (event: Event) => {
                let result: any =  event.returnValue;
                observable = observer.next(result);
                descriptor.value = function () {
                    console.log(`Calling "${propertyKey}" with`, arguments, target);
                    // arguments [0] = asyncMessage
                    // arguments [1] = observable
                    oldValue.apply(null, [asyncMessage, observable]);
                }
            })
        })
    }
}


export class MainComunicator {

    public receiveFromRenderer(): Observable<any> {
        return arguments[1];
    }

}



export function responseToRenderer(asyncMessage: string, asyncReply: string, funct: Function) {
    // TODO: responseToRenderer
}
