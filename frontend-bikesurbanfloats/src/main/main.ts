import { app, BrowserWindow, shell } from 'electron';
import * as path from 'path';
import { format as urlFormat } from 'url';
import { Settings } from './settings';
import { HistoryReader } from './util';

namespace Main {
    let window: Electron.BrowserWindow | null;

    function createWindow() {
        window = new BrowserWindow({ width: 800, height: 600 });

        window.loadURL(urlFormat({
            pathname: path.join(app.getAppPath(), 'frontend', 'index.html'),
            protocol: 'file',
            slashes: true
        }));

        window.on('closed', () => window = null);

        window.webContents.on('will-navigate', (event, url) => {
            event.preventDefault(); // prevents dragging images or other documents into browser window
            shell.openExternal(url); // opens links (or dragged documents) in external browser
        });

        if (process.env.target === 'development') {
            window.webContents.openDevTools();
        }
    }

    export function init() {
        app.on('ready', async () => {
            HistoryReader.enableIpc();
            Settings.enableIpc();
            createWindow();
        });

        app.on('window-all-closed', () => {
            if (process.platform !== 'darwin') app.quit();
        });

        app.on('activate', () => {
            if (window === null) createWindow();
        });

    }
}

Main.init();
