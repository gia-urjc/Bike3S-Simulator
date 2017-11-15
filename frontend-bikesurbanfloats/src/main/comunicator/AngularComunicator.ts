import {MainComunicator, Receive} from './MainComunicator'
import {Observable } from 'rxjs/Observable';

export class AngularComunicator extends MainComunicator {

    @Receive('message-receive-from-frontend')
    receiveFromRenderer(): Observable<any> {
        // TODO test and improve method
        return super.receiveFromRenderer();
    }
}
