export interface SettingsLayerEntry {
    key: string,
    enabled: boolean,
}

export interface SettingsLayers {
    [name: string]: SettingsLayerEntry
}

export interface SettingsTree {
    layers: SettingsLayers,
}
