import { HistoryEntitiesJson, HistoryTimeEntry } from '../../shared/history';
import { JsonValue } from '../../shared/util';
import {CoreSimulatorArgs, UserGeneratorArgs} from "../../shared/BackendInterfaces";
import {JsonInfo, default as JsonLoader, JsonSchemaGroup} from "../../main/json-loader/JsonLoader";
import { CsvArgs } from '../../main/util/CsvGeneratorController';

export interface HistoryAjax {
    init(path: string): Promise<void>;
    getEntities(type: string): Promise<HistoryEntitiesJson>;
    numberOFChangeFiles(): Promise<number>;
    previousChangeFile(): Promise<Array<HistoryTimeEntry>>;
    nextChangeFile(): Promise<Array<HistoryTimeEntry>>;
    getChangeFile(n: number): Promise<Array<HistoryTimeEntry>>;
    closeHistory(): Promise<void>;
}

export interface FormSchemaAjax {
    init(): Promise<void>;
    getSchemaFormEntryPointAndUserTypes(): Promise<any>;
    getSchemaByTypes(dataTypes: any): Promise<any>;
    getStationSchema(): Promise<any>;
    getGlobalSchema(): Promise<any>;
}

export interface JsonLoaderAjax {
    init(): Promise<void>;
    getAllSchemas(): Promise<JsonSchemaGroup>;
    loadJson(path: string): Promise<any>;
    writeJson(jsonInfo: JsonInfo): Promise<boolean>;
    close(): Promise<void>;
}

export interface BackendAjax {
    init(): Promise<void>;
    generateUsers(args: UserGeneratorArgs): Promise<void>;
    simulate(args: CoreSimulatorArgs): Promise<void>;
    cancelSimulation(): Promise<void>;
    closeBackend(): Promise<void>;
}

export interface CsvGeneratorAjax {
    init(): Promise<void>;
    writeCsv(args: CsvArgs): Promise<void>;
    close(): Promise<void>;
}

export interface SettingsAjax {
    get(property: string): Promise<any>;
    set(property: string, value: JsonValue): Promise<void>;
}

export interface AjaxProtocol {
    history: HistoryAjax;
    settings: SettingsAjax;
    formSchema: FormSchemaAjax;
    backend: BackendAjax;
    jsonLoader: JsonLoaderAjax;
    csvGenerator: CsvGeneratorAjax;
}
