import { app, BrowserWindow, shell } from 'electron';
import { join } from 'path';
import { format as urlFormat } from 'url';
import { settingsPathGenerator } from '../shared/settings';
import { ReservationsPerUser } from "./dataAnalysis/absoluteValues/analysisData/users/ReservationsPerUser";
import { ReservationsIterator } from "./dataAnalysis/absoluteValues/systemDataIterators/ReservationsIterator";
import { Settings } from './settings';
import { HistoryReader } from './util';

namespace Main {
    let window: Electron.BrowserWindow | null;

    function createWindow() {
        window = new BrowserWindow({ width: 800, height: 600 });

        window.loadURL(urlFormat({
            pathname: join(app.getAppPath(), 'frontend', 'index.html'),
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

            createWindow();
        });

        app.on('window-all-closed', async () => {
            await Settings.write();
            if (process.platform !== 'darwin') app.quit();
        });

        app.on('activate', () => {
            if (window === null) createWindow();
        });

    }
    
    export async function ptm() {
        console.log('HOLA MUNDO');
        let values = await ReservationsPerUser.create('history');
        console.log('values created');
        let it = await ReservationsIterator.create('history');
        console.log('iterator created');
        it.subscribe(values);
        it.calculateReservations();
        console.log('user 25:  ', values.getBikeFailedReservationsOfUser(25));
    } 
   
    
}
  
Main.init();
Main.ptm();

