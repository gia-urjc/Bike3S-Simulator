import { Injectable } from '@angular/core';
import { MainCommunicator } from '../communicator/MainCommunicator';

@Injectable()
export class TestService {

    constructor(private mainComunicator: MainCommunicator) {}

    public getTest(id: number): Promise<number> {
        return this.mainComunicator.getData('test', 0);
    }
}
