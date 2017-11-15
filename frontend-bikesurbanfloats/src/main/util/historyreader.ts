import * as path from 'path';
import * as fs from 'fs-extra';
import * as AJV from 'ajv';

import { app } from 'electron';

export class HistoryReader {

    private readPath: string;
    private schemaPath: string;
    private ajv: AJV.Ajv;

    constructor(readPath: string) {
        this.readPath = path.join(app.getAppPath(), readPath);
        this.schemaPath = path.join(app.getAppPath(), 'schema');
        this.ajv = new AJV();
    }

    async readEntities(): Promise<any> {
        const schema = await fs.readJson(path.join(this.schemaPath, 'history/entitylist.json'));
        const json = await fs.readJson(path.join(this.readPath, 'entities.json'));

        if (!this.ajv.validate(schema, json)) {
            throw new Error(this.ajv.errorsText());
        }

        return json;
    }
}