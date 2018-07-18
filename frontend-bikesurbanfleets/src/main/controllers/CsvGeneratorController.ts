import { app } from "electron";
import * as paths from 'path';
import { IpcUtil, Channel } from "../util";
import { DataGenerator } from "../dataAnalysis/analysis/generators/DataGenerator";

export interface CsvArgs {
    historyPath: string;
    csvPath: string;
}

export default class CsvGeneratorController {

    private static channels: Array<Channel>;
    private schemaPath = paths.join(app.getAppPath(), 'schema');

    private static create(): CsvGeneratorController {
        return new CsvGeneratorController();
    }

    public static async enableIpc(): Promise<void> {
        IpcUtil.openChannel('csv-generator-init', async () => {
            let csvGenerator = this.create();

            this.channels = [
                new Channel('csv-generator-write', async (args: CsvArgs) => csvGenerator.writeCsv(args))
            ];

            this.channels.forEach((channel) => IpcUtil.openChannel(channel.name, channel.callback));

            IpcUtil.openChannel('csv-generator-close', async () => {
                IpcUtil.closeChannels('csv-generator-close', ...this.channels.map((channel) => channel.name));
                this.enableIpc();
            });

            IpcUtil.closeChannel('csv-generator-init');
        });
    }

    public static async stopIpc(): Promise<void> {
        if(this.channels !== undefined) {
            IpcUtil.closeChannels('csv-generator-close', ...this.channels.map((channel) => channel.name));
            this.enableIpc();
        }
    }

    private async writeCsv(args: CsvArgs): Promise<void> {
        let generator: DataGenerator = await DataGenerator.create(args.historyPath, args.csvPath, this.schemaPath);
        return;
    }

}