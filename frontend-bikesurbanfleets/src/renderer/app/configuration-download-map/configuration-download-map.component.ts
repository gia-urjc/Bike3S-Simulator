import { Component, Input, Inject } from "@angular/core";
import { NgbActiveModal } from "@ng-bootstrap/ng-bootstrap";
import { AjaxProtocol } from "../../ajax/AjaxProtocol";
import { BoundingBox } from "../../../shared/ConfigurationInterfaces";
const { ipcRenderer } = (window as any).require('electron');
import * as $ from "jquery";

@Component({
    selector: 'configuration-download-map',
    template: require('./configuration-download-map.component.html'),
    styles: [require('./configuration-download-map.component.css')]
})
export class ConfDownMapComponent {

    @Input()
    path: string;

    @Input()
    boundingBox: BoundingBox;

    isError: boolean;
    stopped: boolean;
    isDownloading: boolean;
    downloadSpeed: string;
    totalDownloaded: string;
    message: string;

    constructor(@Inject('AjaxProtocol') private ajax: AjaxProtocol,
                public activeModal: NgbActiveModal) {
    }

    async ngOnInit() {

        ipcRenderer.on('download-speed' , (event: Event, data: number) => {
            this.downloadSpeed = Number(data / 1024).toFixed(2);
            $('#download-message').trigger('click');
        });
        ipcRenderer.on('total-download', (event: Event, data: number) => {
            this.totalDownloaded = Number(data / 1048576).toFixed(2);
            $('#download-message').trigger('click');
        });

        this.isDownloading = true;
        this.stopped = false;
        await this.ajax.mapDownloader.init();
        try {
            await this.ajax.mapDownloader.download({path: this.path, bbox: this.boundingBox});
            this.isDownloading = false;
            this.isError = false;
        }
        catch(error) {
            this.isDownloading = false;
            this.isError = true;
            this.message = error;
        }
    }

    async ngOnDestroy() {
        this.ajax.mapDownloader.close();
    }

    close() {
        this.activeModal.close();
    }

    cancel() {
        this.ajax.mapDownloader.cancel();
        this.stopped = true;
        this.activeModal.close();
    }


}