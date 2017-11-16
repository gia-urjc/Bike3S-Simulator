import {AngularCommunicator} from '../communicator/AngularCommunicator'

export class TestController {

    private test = ['One', 'Two', 'Three'];
    private ac: AngularCommunicator = new AngularCommunicator();

    constructor() {
        this.init();
    }

    // Get methods
    public getTest() {
        this.ac.createGetDataService('test', (arg: number) => {
            return this.test[arg];
        })
    }

    private init() {
        this.getTest();
    }
}
