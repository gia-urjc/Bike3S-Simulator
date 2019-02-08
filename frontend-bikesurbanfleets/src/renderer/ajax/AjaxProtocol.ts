import { HistoryEntitiesJson, HistoryTimeEntry } from '../../shared/history';
import { JsonValue } from '../../shared/util';
import {CoreSimulatorArgs, UserGeneratorArgs} from "../../shared/BackendInterfaces";
import { MapDownloadArgs, JsonFileInfo } from '../../shared/ConfigurationInterfaces';
import { JsonSchemaGroup, JsonLayoutGroup } from '../../main/controllers/JsonLoaderController';
import { CsvArgs } from '../../main/controllers/CsvGeneratorController';

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
    getRecommenderTypesSchema(): Promise<any>;
    getRecommenderSchemaByType(type: string): Promise<any>;
}

export interface JsonLoaderAjax {
    init(): Promise<void>;
    getAllSchemas(): Promise<JsonSchemaGroup>;
    getAllLayouts(): Promise<JsonLayoutGroup>;
    loadJsonGlobal(path: string): Promise<any>;
    loadJsonEntryPoints(path: string): Promise<any>;
    loadJsonStations(path: string): Promise<any>;
    saveJsonGlobal(jsonInfo: JsonFileInfo): Promise<boolean>;
    saveJsonEntryPoints(jsonInfo: JsonFileInfo): Promise<boolean>;
    saveJsonStations(jsonInfo: JsonFileInfo): Promise<boolean>;
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
    mapDownloader: MapDownloaderAjax;
}

export interface MapDownloaderAjax {
    init(): Promise<void>;
    download(args: MapDownloadArgs): Promise<void>;
    cancel(): Promise<void>;
    close(): Promise<void>;
}
