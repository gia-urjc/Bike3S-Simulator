import { app } from 'electron';
import { outputJson, readJson } from 'fs-extra';
import { isEqual } from 'lodash';
import * as osPath from 'path';
import { defaultSettings } from '../../shared/settings';
import { SettingsTree } from '../../shared/settings/definitions';
import { JsonValue, Tree } from '../../shared/util';
import IpcUtil from '../util/IpcUtil';

export namespace Settings {

    const file = osPath.join(app.getPath('userData'), 'settings.json');
    let data: SettingsTree;

    async function read(): Promise<void> {
        try {
            data = await readJson(file);
        } catch {
            data = defaultSettings;
        }
    }

    export async function write(): Promise<void> {
        if (data) {
            await outputJson(file, data);
        }
    }

    export async function get(property: string): Promise<any> {
        if (!data) await read();
        return Tree.get(data, property);
    }

    export async function set(property: string, value: JsonValue): Promise<void> {
        if (!isEqual(value, Tree.get(data, property))) {
            Tree.set(data, property, value);
            await write();
        }
    }

    export function enableIpc() {
        IpcUtil.openChannel('settings-get', get);
        IpcUtil.openChannel('settings-set', set);
    }
}
