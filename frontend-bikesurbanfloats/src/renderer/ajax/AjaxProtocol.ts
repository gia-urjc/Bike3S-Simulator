import { EntitiesJson } from '../../shared/generated/EntitiesJson';
import { TimeentriesJson } from '../../shared/generated/TimeentriesJson';
import { JsonValue } from '../../shared/util';

export interface HistoryAjax {
    init(path: string): Promise<void>,
    readEntities(): Promise<EntitiesJson>,
    numberOFChangeFiles(): Promise<number>,
    previousChangeFile(): Promise<TimeentriesJson>,
    nextChangeFile(): Promise<TimeentriesJson>,
}

export interface SettingsAjax {
    get(property: string): Promise<any>,
    set(property: string, value: JsonValue): Promise<void>,
}

export interface AjaxProtocol {
    history: HistoryAjax,
    settings: SettingsAjax,
}
