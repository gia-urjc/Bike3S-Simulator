import { JsonArray, JsonObject, JsonValue } from '../../shared/util';

export interface HistoryAjax {
    init(path: string): Promise<void>,
    readEntities(): Promise<JsonObject>,
    numberOFChangeFiles(): Promise<number>,
    previousChangeFile(): Promise<JsonArray>,
    nextChangeFile(): Promise<JsonArray>,
}

export interface SettingsAjax {
    get(property: string): Promise<any>,
    set(property: string, value: JsonValue): Promise<void>,
}

export interface AjaxProtocol {
    history: HistoryAjax,
    settings: SettingsAjax,
}
