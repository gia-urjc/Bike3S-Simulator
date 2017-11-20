import * as paths from 'path';
import * as fs from 'fs-extra';
import * as AJV from 'ajv';

import { app, ipcMain } from 'electron';
import { without } from 'lodash';
import { IpcUtil } from './index';

export default class HistoryReader {

    private static schemaPath: string;
    private static entityFileSchema: object;
    private static changeFileSchema: object;
    private static ajv: AJV.Ajv;

    private historyPath: string;
    private changeFiles: Array<string>;
    private currentIndex: number;

    static async create(path: string): Promise<HistoryReader> {
        this.schemaPath = this.schemaPath || paths.join(app.getAppPath(), 'schema');
        this.ajv = this.ajv || new AJV();
        this.changeFileSchema = this.changeFileSchema || await fs.readJson(paths.join(this.schemaPath, 'history/eventlist.json'));
        this.entityFileSchema = this.entityFileSchema || await fs.readJson(paths.join(this.schemaPath, 'history/entitylist.json'));

        let reader = new HistoryReader(path);

        reader.changeFiles = without(await fs.readdir(reader.historyPath), 'entities.json');

        return reader;
    }

    static enableIpc(): void {
        IpcUtil.openChannel('history-init', async (historyPath: string) => {
            const reader = await this.create(historyPath);

            IpcUtil.openChannel('history-entities', async () => await reader.readEntities());
            IpcUtil.openChannel('history-previous', async () => await reader.previousChangeFile());
            IpcUtil.openChannel('history-next', async () => await reader.nextChangeFile());
            IpcUtil.openChannel('history-nchanges', async () => await reader.numberOfChangeFiles());

            IpcUtil.openChannel('history-close', async () => {
                IpcUtil.closeChannels('history-entities', 'history-previous', 'history-next', 'history-nchanges', 'history-close');
            });

            IpcUtil.closeChannel('history-init');

            return 'success';
        });
    }

    private constructor(path: string) {
        this.currentIndex = 0;
        this.historyPath = paths.join(app.getAppPath(), path);
    }

    async readEntities(): Promise<object> {
        const entities = await fs.readJson(paths.join(this.historyPath, 'entities.json'));

        if (!HistoryReader.ajv.validate(HistoryReader.entityFileSchema, entities)) {
            throw new Error(HistoryReader.ajv.errorsText());
        }

        return entities;
    }

    async previousChangeFile(): Promise<object> {
        if (this.currentIndex === 0) {
            throw new Error(`No previous change file available!`);
        }

        const file = await fs.readJson(paths.join(this.historyPath, this.changeFiles[this.currentIndex--]));

        if (!HistoryReader.ajv.validate(HistoryReader.changeFileSchema, file)) {
            throw new Error(HistoryReader.ajv.errorsText());
        }

        return file;
    }

    async nextChangeFile(): Promise<object> {
        if (this.currentIndex === this.changeFiles.length - 1) {
            throw new Error(`No next change file available!`);
        }

        const file = await fs.readJson(paths.join(this.historyPath, this.changeFiles[this.currentIndex++]));

        if (!HistoryReader.ajv.validate(HistoryReader.changeFileSchema, file)) {
            throw new Error(HistoryReader.ajv.errorsText());
        }

        return file;
    }

    numberOfChangeFiles(): number {
        return this.changeFiles.length;
    }
}