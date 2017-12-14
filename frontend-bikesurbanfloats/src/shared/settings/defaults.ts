import { SettingsTree } from './definitions';


const defaultSettings: SettingsTree = {
    layers: {
        thunderforest: {
            key: '',
            enabled: false,
        },
        mapbox: {
            key: '',
            enabled: false,
        }
    },
};

export default defaultSettings;
