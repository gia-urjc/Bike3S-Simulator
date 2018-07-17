import * as AJV from 'ajv';
import * as fs from 'fs-extra';
import * as paths from 'path';
import { app } from 'electron';
import { without } from 'lodash';
import { HistoryEntitiesJson, HistoryTimeEntry } from '../../shared/history';
import { IpcUtil, Channel } from '../util';

interface TimeRange {
    start: number;
    end: number;
}

export default class HistoryReaderController {

    private static ajv = new AJV({
        $data: true,
        // allErrors: true,
        verbose: true,
    });

    private static entityFileSchema: any;
    private static changeFileSchema: any;
    private static channels: Array<Channel>;

    private historyPath: string;
    private changeFiles: Array<string>;
    private currentIndex: number;

    static async create(path: string, schemaPath?:string|null): Promise<HistoryReaderController> {
        let reader = new HistoryReaderController(path);
        if(schemaPath == null) {
            HistoryReaderController.entityFileSchema = fs.readJsonSync(paths.join(app.getAppPath(), 'schema/entities.json'));
            HistoryReaderController.changeFileSchema = fs.readJsonSync(paths.join(app.getAppPath(), 'schema/timeentries.json'));
        }
        else {
            HistoryReaderController.entityFileSchema = fs.readJsonSync(paths.join(schemaPath, 'entities.json'));
            HistoryReaderController.changeFileSchema = fs.readJsonSync(paths.join(schemaPath, 'timeentries.json'));
        }
        console.log(reader.historyPath);
        reader.changeFiles = without(await fs.readdir(reader.historyPath), 'entities').sort((a, b) => {
            const [x, y] = [a, b].map((s) => parseInt(s.split('-')[0]));
            return x - y;
        });
        return reader;
    }

    static enableIpc(): void {
        IpcUtil.openChannel('history-init', async (historyPath: string) => {
            const reader = await this.create(historyPath);

            this.channels = [
                new Channel('history-entities', async (type) => await reader.getEntities(type)),
                new Channel('history-get', async (n) => await reader.changeFile(n)),
                new Channel('history-previous', async () => await reader.previousChangeFile()),
                new Channel('history-next', async () => await reader.nextChangeFile()),
                new Channel('history-nchanges', async () => reader.numberOfChangeFiles),
                new Channel('history-range', async () => reader.timeRange)
            ];

            this.channels.forEach((channel) => IpcUtil.openChannel(channel.name, channel.callback));

            IpcUtil.openChannel('history-close', async () => {
                IpcUtil.closeChannels('history-close', ...this.channels.map((channel) => channel.name));
                this.enableIpc();
            });

            IpcUtil.closeChannel('history-init');
        });
    }

    static stopIpc(): void {
        if(this.channels !== undefined) {
            IpcUtil.closeChannels('history-close', ...this.channels.map((channel) => channel.name));
            this.enableIpc();
        }
    }

    private constructor(path: string) {
        this.currentIndex = -1;
        this.historyPath = path;
    }

    clipToRange(start: number, end: number = this.timeRange.end): void {
        if (this.currentIndex !== -1) {
            throw new Error(`Clipping is only allowed before reading any change file!`);
        }

        if (start < this.timeRange.start) {
            throw new Error(`start may not be lower than current range`);
        }

        if (end > this.timeRange.end) {
            throw new Error(`end may not be higher than current range`);
        }

        this.changeFiles = this.changeFiles.filter((file) => {
            const [fileStart, fileEnd] = file.split('_')[0].split('-').map(parseInt);
            return end >= fileStart && start <= fileEnd;
        });
    }

    async getEntities(type: string): Promise<HistoryEntitiesJson> {
        const entities = await fs.readJson(paths.join(this.historyPath, `entities/${type}.json`));

        if (!HistoryReaderController.ajv.validate(HistoryReaderController.entityFileSchema, entities)) {
            throw new Error(HistoryReaderController.ajv.errorsText());
        }

        return entities;
    }

    async previousChangeFile(): Promise<Array<HistoryTimeEntry>>  {
        if (this.currentIndex <= 0) {
            throw new Error(`No previous change file available!`);
        }

        const file = await fs.readJson(paths.join(this.historyPath, this.changeFiles[--this.currentIndex]));

        if (!HistoryReaderController.ajv.validate(HistoryReaderController.changeFileSchema, file)) {
            throw new Error(HistoryReaderController.ajv.errorsText());
        }

        return file;
    }

    async nextChangeFile(): Promise<Array<HistoryTimeEntry>> {
        if (this.currentIndex === this.changeFiles.length - 1) {
            throw new Error(`No next change file available!`);
        }

        const file = await fs.readJson(paths.join(this.historyPath, this.changeFiles[++this.currentIndex]));

        if (!HistoryReaderController.ajv.validate(HistoryReaderController.changeFileSchema, file)) {
            throw new Error(HistoryReaderController.ajv.errorsText());
        }

        return file;
    }

    async changeFile(n: number): Promise<Array<HistoryTimeEntry>> {
        if (n < 0 || n >= this.numberOfChangeFiles) {
            throw new Error(`Change file not available! Got ${n}, need n ∈ [0, ${this.numberOfChangeFiles})`);
        }

        const file = await fs.readJson(paths.join(this.historyPath, this.changeFiles[n]));

        if (!HistoryReaderController.ajv.validate(HistoryReaderController.changeFileSchema, file)) {
            throw new Error(HistoryReaderController.ajv.errorsText());
        }

        return file;
    }

    get numberOfChangeFiles(): number {
        return this.changeFiles.length;
    }

    get timeRange(): TimeRange {
        const range = [
            this.changeFiles[0],
            this.changeFiles.slice(-1)[0]
        ].map((file, index) => parseInt(file.split('_')[0].split('-')[index]));

        return {
            start: range[0],
            end: range[1]
        };
    }
}
