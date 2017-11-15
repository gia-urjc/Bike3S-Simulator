import { Component, OnInit } from '@angular/core';
import { MainComunicator } from './services/MainComunicator';

@Component({
    selector: 'my-app',
    template: require('./app.component.html'),
    styles: [require('./app.component.css')]
})
export class AppComponent implements OnInit {

    test: string[];

    constructor(private mainComunicatorService: MainComunicator) {
        mainComunicatorService.init();
    }
    
    ngOnInit() {
        this.mainComunicatorService.getDataTest(0).subscribe((data) => {
            this.test = data;
        })
    }
}
