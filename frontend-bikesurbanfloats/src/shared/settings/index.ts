import SettingsDefaults from './defaults';

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
    }, SettingsDefaults as SettingsPath<typeof SettingsDefaults>), {
        get(_, key, proxy) {
            parts.push(key);
            return proxy;
        }
    })
}

export {
    SettingsDefaults,
    settingsPath,
}
