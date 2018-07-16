import { Component, Input, Inject } from "@angular/core";
import { NgbActiveModal } from "@ng-bootstrap/ng-bootstrap";
import { AjaxProtocol } from "../../ajax/AjaxProtocol";
import { BoundingBox } from "../../../shared/ConfigurationInterfaces";

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
    isDownloading: boolean;
    message: string;

    constructor(@Inject('AjaxProtocol') private ajax: AjaxProtocol,
                public activeModal: NgbActiveModal) {
    }

    async ngOnInit() {
        await this.ajax.mapDownloader.init();
        this.isDownloading = true;
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


}