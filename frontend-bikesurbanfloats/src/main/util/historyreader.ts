import * as paths from 'path';
import * as fs from 'fs-extra';
import * as AJV from 'ajv';

import { app } from 'electron';
import { without } from 'lodash';

export class HistoryReader {

    private static historyPath: string;
    private static schemaPath: string;
    private static ajv: AJV.Ajv;
    private static changeFiles: Array<string>;

    static async init(path: string): Promise<void> {
        HistoryReader.historyPath = paths.join(app.getAppPath(), path);
        HistoryReader.schemaPath = paths.join(app.getAppPath(), 'schema');
        HistoryReader.ajv = new AJV();
        HistoryReader.changeFiles = without(await fs.readdir(HistoryReader.historyPath), 'entities.json');
        console.log(HistoryReader.changeFiles);
    }

    static async readEntities(): Promise<object> {
        const schema = await fs.readJson(paths.join(HistoryReader.schemaPath, 'history/entitylist.json'));
        const entities = await fs.readJson(paths.join(HistoryReader.historyPath, 'entities.json'));

        if (!HistoryReader.ajv.validate(schema, entities)) {
            throw new Error(HistoryReader.ajv.errorsText());
        }

        return entities;
    }
}