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
            "background": "linear-gradient(to top, #6a85b6 0%, #bac8e0 100%)", //Gradient color
            "overflow-y": "hidden"
        });
    }

    openWindow(windowName: String) {
        ipcRenderer.send(windowName);
    }

}