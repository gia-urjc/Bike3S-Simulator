import { app, BrowserWindow, shell } from 'electron';
import { join } from 'path';
import { format as urlFormat } from 'url';
import { settingsPathGenerator } from '../shared/settings';
import { Settings } from './settings';
import { HistoryReader } from './util';
import SchemaFormGenerator from "./configuration/SchemaFormGenerator";
import { AbsoluteValue } from "./dataAnalysis/analysis/absoluteValues/AbsoluteValue";
import { Data } from "./dataAnalysis/analysis/absoluteValues/Data";
import { RentalAndReturnAbsoluteValue } from "./dataAnalysis/analysis/absoluteValues/rentalsAndReturns/RentalAndReturnAbsoluteValue";
import { RentalsAndReturnsPerStation } from "./dataAnalysis/analysis/absoluteValues/rentalsAndReturns/RentalsAndReturnsPerStation";
import { RentalsAndReturnsPerUser } from "./dataAnalysis/analysis/absoluteValues/rentalsAndReturns/RentalsAndReturnsPerUser";
import { ReservationsPerStation } from "./dataAnalysis/analysis/absoluteValues/reservations/ReservationsPerStation";
import { ReservationsPerUser } from "./dataAnalysis/analysis/absoluteValues/reservations/ReservationsPerUser";
import { BikesPerStation, StationBikesPerTimeList } from "./dataAnalysis/analysis/absoluteValues/time/BikesPerStation";
import { EmptyStationInfo } from "./dataAnalysis/analysis/absoluteValues/time/EmptyStationInfo";
import { RentalAndReturnCalculator } from "./dataAnalysis/analysis/calculators/RentalAndReturnCalculator";
import { ReservationCalculator } from "./dataAnalysis/analysis/calculators/ReservationCalculator";
import { DataGenerator } from "./dataAnalysis/analysis/generators/DataGenerator";
import { SystemReservations } from "./dataAnalysis/analysis/systemEntities/SystemReservations";
import { SystemStations } from "./dataAnalysis/analysis/systemEntities/SystemStations";
import { SystemUsers } from "./dataAnalysis/analysis/systemEntities/SystemUsers";

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
    
<<<<<<< HEAD
    export async function testRentals(): Promise<void> {
        let s: SystemStations = new SystemStations();
        let res: SystemReservations = new SystemReservations();
        let u: SystemUsers = new SystemUsers(); 
        try {
        await s.init('history');
        await res.init('history');
        await u.init('history');
        } catch(error) { console.log(error); }
        let d: BikesPerStation = new BikesPerStation();
        d.setReservations(res.getReservations());
        try {
            d.init(s.getStations());
            let c: RentalAndReturnCalculator = new RentalAndReturnCalculator('history');
            c.subscribe(d);
            await c.calculate(); 
            let v: StationBikesPerTimeList | undefined = d.getStations().get(7);
            if (v !== undefined) {
                v.getList().forEach( (info) => console.log(info.time+' '+info.availableBikes));
                let info: EmptyStationInfo = new EmptyStationInfo(d);
                await info.init();
                let  stations: Data = info.getData();
                console.log(stations.absoluteValues.get(18));
            }
        } catch(e) { console.log(e); }
    }
       
=======
    export async function test() {
       try {
            let data: DataGenerator = await DataGenerator.generate('history', 'csvFiles');
        }
        catch(error) {
           console.log('Error: ', error);
        }
        
    }
>>>>>>> d86b148f5d966d645a819dde4afc777d22832467
}
  
Main.init();
Main.testRentals();
