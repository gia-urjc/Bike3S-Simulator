import { IpcUtil } from '../util'

export class TestController {

    private test = ['One', 'Two', 'Three'];

    constructor() {
        this.init();
    }

    // Get methods
    public getTest() {
        IpcUtil.openChannel('test', async (arg: number) => {
            return this.test[arg];
        })
    }

    private init() {
        this.getTest();
    }
}
