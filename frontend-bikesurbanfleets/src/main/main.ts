import { app, BrowserWindow, shell } from 'electron';
import { join } from 'path';
import { format as urlFormat } from 'url';
import { settingsPathGenerator } from '../shared/settings';
import { Settings } from './settings';
import { HistoryReader } from './util';
import SchemaFormGenerator from "./configuration/SchemaFormGenerator";
import { DataGenerator } from "./dataAnalysis/analysis/generators/DataGenerator";
import { ipcMain, ipcRenderer } from 'electron';

namespace Main {
    let visualization: Electron.BrowserWindow | null;
    let menu: Electron.BrowserWindow | null;
    let simulate: Electron.BrowserWindow | null;

    export function initWindowsListeners() {
        ipcMain.on('open-visualization', (event: any, arg: any) => {
            createVisualizationWindow();
        });

        ipcMain.on('open-simulate', (event: any, arg: any) => {
            createSimulateWindow();
        })
    }

    /*===================
     *
     *  MENU WINDOW
     *
     ===================*/

    function createMenuWindow() {
        menu = new BrowserWindow({ width: 300, height: 650, resizable: false, fullscreenable: true});

        menu.loadURL(urlFormat({
            pathname: join(app.getAppPath(), 'frontend', 'index.html'),
            protocol: 'file',
            slashes: true
        }));

        menu.on('closed', () => visualization = null);

        menu.webContents.on('will-navigate', (event, url) => {
            event.preventDefault(); // prevents dragging images or other documents into browser window
            shell.openExternal(url); // opens links (or dragged documents) in external browser
        });

        //menu.webContents.openDevTools();

        menu.loadURL('file://' + app.getAppPath() + '/frontend/index.html#/menu');
    }

    export function initMenu() {
        HistoryReader.enableIpc();
        Settings.enableIpc();

        app.on('ready', async () => {
            //HistoryReader.enableIpc();
            //Settings.enableIpc();
            //SchemaFormGenerator.enableIpc();

            if (process.env.target === 'development') {
                const extensions = await Settings.get(settingsPathGenerator().development.extensions());
                Object.entries(extensions).forEach(([name, extensionPath]) => {
                    if (!extensionPath) {
                        console.log(`Empty path for browser extension ${name}`);
                        return;
                    }

                    try {
                        BrowserWindow.addDevToolsExtension(extensionPath);
                    } catch (e) {
                        console.log(`Couldn't load browser extension ${name} from path ${extensionPath}`);
                    }
                });
            }

            createMenuWindow();
            if (menu !== null) {
                menu.setTitle("Bike Sharing System Simulator");
            }
        });

        app.on('window-all-closed', async () => {
            await Settings.write();
            if (process.platform !== 'darwin') app.quit();
        });

        app.on('activate', () => {
            if (menu === null) createMenuWindow();
        });
    }

    /*===================
     *
     *  VISUALITATION WINDOW
     *
     ===================*/


    function createVisualizationWindow() {
        visualization = new BrowserWindow({ width: 800, height: 600 });

        visualization.loadURL(urlFormat({
            pathname: join(app.getAppPath(), 'frontend', 'index.html'),
            protocol: 'file',
            slashes: true
        }));

        visualization.on('closed', async () => {
            visualization = null;
            HistoryReader.stopIpc();
        });

        visualization.webContents.on('will-navigate', (event, url) => {
            event.preventDefault(); // prevents dragging images or other documents into browser window
            shell.openExternal(url); // opens links (or dragged documents) in external browser
        });

        if (process.env.target === 'development') {
            visualization.webContents.openDevTools();
        }

        visualization.loadURL('file://' + app.getAppPath() + '/frontend/index.html#/visualization');
    }

    /*===================
     *
     *  SIMULATE WINDOW
     *
     ===================*/

    function createSimulateWindow() {
        simulate = new BrowserWindow({ width: 800, height: 600 });

        simulate.loadURL(urlFormat({
            pathname: join(app.getAppPath(), 'frontend', 'index.html'),
            protocol: 'file',
            slashes: true
        }));

        simulate.on('closed', async () => {
            visualization = null;
            HistoryReader.stopIpc();
        });

        simulate.webContents.on('will-navigate', (event, url) => {
            event.preventDefault(); // prevents dragging images or other documents into browser window
            shell.openExternal(url); // opens links (or dragged documents) in external browser
        });

        if (process.env.target === 'development') {
            simulate.webContents.openDevTools();
        }

        simulate.loadURL('file://' + app.getAppPath() + '/frontend/index.html#/simulate');
    }
    
    export async function test() {
       try {
            let data: DataGenerator = await DataGenerator.generate('history', 'csvFiles');
        }
        catch(error) {
           console.log('Error: ', error);
        }
        
    }
}

Main.initMenu();
Main.initWindowsListeners();
