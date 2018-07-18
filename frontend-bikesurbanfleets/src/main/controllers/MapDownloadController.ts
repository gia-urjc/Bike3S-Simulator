import * as fs from 'fs-extra';
import { MapDownloadArgs } from "../../shared/ConfigurationInterfaces";
import { Channel, IpcUtil } from '../util';
import {Main} from "../main";
const request = require('request');
const progress = require('request-progress');

interface DownloadState {
    percentage: number;
    speed: number;
    size: {
        total: number;
        transferred: number;
    };
    time: {
        elapsed: number;
        remaining: number;
    };
}

export default class MapDownloadController {

    private static channels: Channel[] = [];
    private BASE_URL = "http://overpass-api.de/api/interpreter?data=";
    private readonly window: Electron.BrowserWindow | null;
    private request: any;

    private static create(): MapDownloadController {
        return new MapDownloadController();
    }

    public static enableIpc() {
        IpcUtil.openChannel('map-download-init', async () => {
            const mapDController = this.create();

            this.channels = [
                new Channel('map-download-get', async (args: MapDownloadArgs) => await mapDController.download(args)),
                new Channel('map-download-cancel',  async () => await mapDController.cancel())
            ];

            this.channels.forEach((channel) => IpcUtil.openChannel(channel.name, channel.callback));

            IpcUtil.openChannel('map-download-close', async () => {
                IpcUtil.closeChannels('map-download-close', ...this.channels.map((channel) => channel.name));
                this.enableIpc();
            });

            IpcUtil.closeChannel('map-download-init');
        });
    }

    constructor() {
        this.window = Main.configuration;
    }
    

    private download(args: MapDownloadArgs): Promise<void> {
        return new Promise((resol, reject) => {
            console.log(args);
            let nlat = args.bbox.northWest.latitude;
            let nlon = args.bbox.southEast.longitude;
            let slat = args.bbox.southEast.latitude;
            let slon = args.bbox.northWest.longitude;
            let url = this.BASE_URL + `(way["highway"](${slat},${slon},${nlat},${nlon});node(w););out;`;
            console.log(url);
            let output = fs.createWriteStream(args.path);
            this.request = request(url);
            progress(this.request)
            .on('progress', (state: DownloadState) => {
                console.log(state);
                this.sendInfoToGui('download-speed', state.speed);
                this.sendInfoToGui('total-download', state.size.transferred);
            })
            .on('error', (err: any) => {
                console.log(err);
                this.request = undefined;
                reject();
            })
            .on('end', () => {
                this.request = undefined;
                resol();
            })
            .pipe(output);
            }
        );
    }

    async cancel(): Promise<void> {
        if(this.request) {
            this.request.abort();
            this.request = undefined;
            return;
        }
        else {
            throw new Error("There's no request to cancel");
        }
    }

    private sendInfoToGui(channel: string, message: string | number): void {
        if(this.window) {
            this.window.webContents.send(channel, message);
        }
    }
}