import defaultSettings from './defaults';

interface PathCall {
    (): string,
}

type SettingsPath<T> = {
    [P in keyof T]: PathCall & SettingsPath<T[P]>
}

function settingsPath() {
    let parts: Array<PropertyKey> = [];
    return new Proxy(Object.assign(function () {
        const path = parts.join('.');
        parts = [];
        return path;
    }, defaultSettings as SettingsPath<typeof defaultSettings>), {
        get(_, key, proxy) {
            parts.push(key);
            return proxy;
        }
    });
}

export {
    defaultSettings,
    settingsPath,
}
