import * as paths from 'path';
import * as fs from 'fs-extra';
import * as AJV from 'ajv';

import { app } from 'electron';
import { without } from 'lodash';

export default class {

    private static historyPath: string;
    private static schemaPath: string;
    private static ajv: AJV.Ajv;
    private static changeFiles: Array<string>;
    private static entityFileSchema: object;
    private static changeFileSchema: object;
    private static currentIndex: number;

    static async init(path: string): Promise<void> {
        this.currentIndex = 0;
        this.historyPath = paths.join(app.getAppPath(), path);
        this.schemaPath = paths.join(app.getAppPath(), 'schema');
        this.ajv = new AJV();
        this.changeFiles = without(await fs.readdir(this.historyPath), 'entities.json');
        this.changeFileSchema = await fs.readJson(paths.join(this.schemaPath, 'history/eventlist.json'));
        this.entityFileSchema = await fs.readJson(paths.join(this.schemaPath, 'history/entitylist.json'));
    }

    static async readEntities(): Promise<object> {
        const entities = await fs.readJson(paths.join(this.historyPath, 'entities.json'));

        if (!this.ajv.validate(this.entityFileSchema, entities)) {
            throw new Error(this.ajv.errorsText());
        }

        return entities;
    }

    static async previousChangeFile(): Promise<object> {
        if (this.currentIndex === 0) {
            throw new Error(`No previous change file available!`);
        }

        const file = await fs.readJson(paths.join(this.historyPath), this.changeFiles[this.currentIndex--]);

        if (!this.ajv.validate(this.changeFileSchema, file)) {
            throw new Error(this.ajv.errorsText());
        }

        return file;
    }

    static async nextChangeFile(): Promise<object> {
        if (this.currentIndex === this.changeFiles.length - 1) {
            throw new Error(`No next change file available!`);
        }

        const file = await fs.readJson(paths.join(this.historyPath), this.changeFiles[this.currentIndex++]);

        if (!this.ajv.validate(this.changeFileSchema, file)) {
            throw new Error(this.ajv.errorsText());
        }

        return file;
    }

    static numberOfChangeFiles(): number {
        return this.changeFiles.length;
    }
}