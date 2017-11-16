import * as paths from 'path';
import * as fs from 'fs-extra';
import * as AJV from 'ajv';

import { app } from 'electron';

export class HistoryReader {

    private historyPath: string;
    private schemaPath: string;
    private ajv: AJV.Ajv;

    constructor(path: string) {
        this.historyPath = paths.join(app.getAppPath(), path);
        this.schemaPath = paths.join(app.getAppPath(), 'schema');
        this.ajv = new AJV();
    }

    async readEntities(): Promise<any> {
        const schema = await fs.readJson(paths.join(this.schemaPath, 'history/entitylist.json'));
        const entities = await fs.readJson(paths.join(this.historyPath, 'entities.json'));

        if (!this.ajv.validate(schema, entities)) {
            throw new Error(this.ajv.errorsText());
        }

        return entities;
    }
}