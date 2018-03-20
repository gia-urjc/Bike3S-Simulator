import { HistoryEntitiesJson, HistoryTimeEntry } from '../../shared/history';
import { JsonValue } from '../../shared/util';
import {CoreSimulatorArgs, UserGeneratorArgs} from "../../shared/BackendInterfaces";

export interface HistoryAjax {
    init(path: string): Promise<void>,
    getEntities(type: string): Promise<HistoryEntitiesJson>,
    numberOFChangeFiles(): Promise<number>,
    previousChangeFile(): Promise<Array<HistoryTimeEntry>>,
    nextChangeFile(): Promise<Array<HistoryTimeEntry>>,
    getChangeFile(n: number): Promise<Array<HistoryTimeEntry>>,
    closeHistory(): Promise<void>
}

export interface FormSchemaAjax {
    init(): Promise<void>,
    getSchemaFormEntryPointAndUserTypes(): Promise<any>,
    getSchemaByTypes(dataTypes: any): Promise<any>,
    getStationSchema(): Promise<any>,
}

export interface BackendAjax {
    init(): Promise<void>,
    generateUsers(args: UserGeneratorArgs): Promise<void>
    simulate(args: CoreSimulatorArgs): Promise<void>
    closeBackend(): Promise<void>
}

export interface SettingsAjax {
    get(property: string): Promise<any>,
    set(property: string, value: JsonValue): Promise<void>,
}

export interface AjaxProtocol {
    history: HistoryAjax,
    settings: SettingsAjax,
    formSchema: FormSchemaAjax,
    backend: BackendAjax
}
