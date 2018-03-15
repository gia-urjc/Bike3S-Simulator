import {Component, OnInit} from "@angular/core";
import * as $ from "jquery";
const { ipcRenderer } = (window as any).require('electron');

@Component({
    selector: 'menu',
    template: require('./menu.component.html'),
    styles: [require('./menu.component.css')],
})
export class MenuComponent implements OnInit{

    ngOnInit(): void {
        $('body').css({
            "background": "linear-gradient(to top, #00467f, #a5cc82)", //Gradient color
            "overflow-y": "hidden"
        });
    }

    openVisualization() {
        ipcRenderer.send('open-visualization');
    }


}