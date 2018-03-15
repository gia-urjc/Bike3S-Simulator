import * as AJV from 'ajv';
import * as fs from 'fs-extra';
import * as paths from 'path';

import { app } from 'electron';
import { without } from 'lodash';

import { HistoryEntitiesJson, HistoryTimeEntry } from '../../shared/history';
import { IpcUtil } from './index';

interface TimeRange {
    start: number,
    end: number
}

class Channel {
    constructor(public name: string, public callback: (data?: any) => Promise<any>) {}
}

export default class HistoryReader {

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

    static async create(path: string, schemaPath?:string|null): Promise<HistoryReader> {
        let reader = new HistoryReader(path, schemaPath);
        if(schemaPath == null) {
            HistoryReader.entityFileSchema = fs.readJsonSync(paths.join(app.getAppPath(), 'schema/entities.json'));
            HistoryReader.changeFileSchema = fs.readJsonSync(paths.join(app.getAppPath(), 'schema/timeentries.json'));;
        }
        else {
            HistoryReader.entityFileSchema = fs.readJsonSync(paths.join(schemaPath, 'entities.json'));
            HistoryReader.changeFileSchema = fs.readJsonSync(paths.join(schemaPath, 'timeentries.json'))
        }
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
                new Channel('history-range', async () => reader.timeRange),
                new Channel('history-restart', async () => reader.restart())
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
        IpcUtil.closeChannels(...this.channels.map((channel) => channel.name));
        this.enableIpc();
    }

    private constructor(path: string, pathSchema?: string|null) {
        this.currentIndex = -1;
        if(pathSchema == null) {
            this.historyPath = paths.join(app.getAppPath(), path);
        }
        else {
            this.historyPath = path;
        }
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

        if (!HistoryReader.ajv.validate(HistoryReader.entityFileSchema, entities)) {
            throw new Error(HistoryReader.ajv.errorsText());
        }

        return entities;
    }

    async previousChangeFile(): Promise<Array<HistoryTimeEntry>>  {
        if (this.currentIndex <= 0) {
            throw new Error(`No previous change file available!`);
        }

        const file = await fs.readJson(paths.join(this.historyPath, this.changeFiles[--this.currentIndex]));

        if (!HistoryReader.ajv.validate(HistoryReader.changeFileSchema, file)) {
            throw new Error(HistoryReader.ajv.errorsText());
        }

        return file;
    }

    async nextChangeFile(): Promise<Array<HistoryTimeEntry>> {
        if (this.currentIndex === this.changeFiles.length - 1) {
            throw new Error(`No next change file available!`);
        }

        const file = await fs.readJson(paths.join(this.historyPath, this.changeFiles[++this.currentIndex]));

        if (!HistoryReader.ajv.validate(HistoryReader.changeFileSchema, file)) {
            throw new Error(HistoryReader.ajv.errorsText());
        }

        return file;
    }

    async changeFile(n: number): Promise<Array<HistoryTimeEntry>> {
        if (n < 0 || n >= this.numberOfChangeFiles) {
            throw new Error(`Change file not available! Got ${n}, need n ∈ [0, ${this.numberOfChangeFiles})`);
        }

        const file = await fs.readJson(paths.join(this.historyPath, this.changeFiles[n]));

        if (!HistoryReader.ajv.validate(HistoryReader.changeFileSchema, file)) {
            throw new Error(HistoryReader.ajv.errorsText());
        }

        return file;
    }

    restart(): void {
        this.currentIndex = -1;
        return;
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
        }
    }
}
