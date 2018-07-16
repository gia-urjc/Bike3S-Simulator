import IpcUtil from "./IpcUtil";
import Channel from "./Channel";
import * as fs from 'fs-extra';
import { MapDownloadArgs } from "../../shared/ConfigurationInterfaces";
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
    
    private static create(): MapDownloadController {
        return new MapDownloadController();
    }

    public static enableIpc() {
        IpcUtil.openChannel('map-download-init', async () => {
            const mapDController = this.create();

            this.channels = [
                new Channel('map-download-get', async (args: MapDownloadArgs) => await mapDController.download(args))
            ];

            this.channels.forEach((channel) => IpcUtil.openChannel(channel.name, channel.callback));

            IpcUtil.openChannel('map-download-close', async () => {
                IpcUtil.closeChannels('map-download-close', ...this.channels.map((channel) => channel.name));
                this.enableIpc();
            });

            IpcUtil.closeChannel('map-download-init');
        });
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
                progress(request(url))
                .on('progress', (state: DownloadState) => {
                    console.log(state);
                })
                .on('error', (err: any) => {
                    console.log(err);
                    reject();
                })
                .on('end', () => {resol();})
                .pipe(output);
            }
        );
    }
}