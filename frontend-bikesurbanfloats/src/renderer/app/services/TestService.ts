import { Injectable } from '@angular/core';
import { IpcUtilRenderer } from '../../util/IpcUtilRenderer';

@Injectable()
export class TestService {

    constructor(private ipc: IpcUtilRenderer) {}

    public getTest(id: number): Promise<number> {
        return this.ipc.getData('test', 0);
    }
}
