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

export class HistoryReader {

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

    static create(path: string, schemaPath?:string|null): HistoryReader {
        let reader = new HistoryReader(path);
        if(schemaPath === null || schemaPath === undefined) {
            HistoryReader.entityFileSchema = fs.readJsonSync(paths.join(app.getAppPath(), 'schema/entities.json'));
            HistoryReader.changeFileSchema = fs.readJsonSync(paths.join(app.getAppPath(), 'schema/timeentries.json'));
        }
        else {
            HistoryReader.entityFileSchema = fs.readJsonSync(paths.join(schemaPath, 'entities.json'));
            HistoryReader.changeFileSchema = fs.readJsonSync(paths.join(schemaPath, 'timeentries.json'));
        }
        console.log(reader.historyPath);
        reader.changeFiles = without(fs.readdirSync(reader.historyPath), 'entities', '.DS_Store', 'final-global-values.json').sort((a, b) => {
            const [x, y] = [a, b].map((s) => parseInt(s.split('-')[0]));
            return x - y;
        });
        return reader;
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

    public getEntities(type: string): HistoryEntitiesJson {
        const entities = fs.readJsonSync(paths.join(this.historyPath, `entities/${type}.json`));

        if (!HistoryReader.ajv.validate(HistoryReader.entityFileSchema, entities)) {
            throw new Error(HistoryReader.ajv.errorsText());
        }

        return entities;
    }

    public previousChangeFile(): Array<HistoryTimeEntry>  {
        if (this.currentIndex <= 0) {
            throw new Error(`No previous change file available!`);
        }

        const file = fs.readJsonSync(paths.join(this.historyPath, this.changeFiles[--this.currentIndex]));

        if (!HistoryReader.ajv.validate(HistoryReader.changeFileSchema, file)) {
            throw new Error(HistoryReader.ajv.errorsText());
        }

        return file;
    }

    public nextChangeFile(): Array<HistoryTimeEntry> {
        if (this.currentIndex === this.changeFiles.length - 1) {
            throw new Error(`No next change file available!`);
        }

        const file = fs.readJsonSync(paths.join(this.historyPath, this.changeFiles[++this.currentIndex]));

        if (!HistoryReader.ajv.validate(HistoryReader.changeFileSchema, file)) {
            throw new Error(HistoryReader.ajv.errorsText());
        }

        return file;
    }

    changeFile(n: number): Array<HistoryTimeEntry> {
        if (n < 0 || n >= this.numberOfChangeFiles) {
            throw new Error(`Change file not available! Got ${n}, need n ∈ [0, ${this.numberOfChangeFiles})`);
        }

        const file = fs.readJsonSync(paths.join(this.historyPath, this.changeFiles[n]));

        if (!HistoryReader.ajv.validate(HistoryReader.changeFileSchema, file)) {
            throw new Error(HistoryReader.ajv.errorsText());
        }

        return file;
    }

    public get numberOfChangeFiles(): number {
        return this.changeFiles.length;
    }

    public get timeRange(): TimeRange {
        const range = [
            this.changeFiles[0],
            this.changeFiles.slice(-1)[0]
        ].map((file, index) => parseInt(file.split('_')[0].split('-')[index]));

        return {
            start: range[0],
            end: range[1]
        };
    }

    getGlobalValues(): any {
        const values = fs.readJsonSync(paths.join(this.historyPath, 'final-global-values.json'));
        return values;
    }
}
