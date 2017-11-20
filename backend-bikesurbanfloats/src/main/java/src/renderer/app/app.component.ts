import { Component, OnInit } from '@angular/core';
import { TestService } from './services/TestService';

@Component({
    selector: 'my-app',
    template: require('./app.component.html'),
    styles: [require('./app.component.css')]
})
export class AppComponent implements OnInit {

    test: number;

    constructor(private testService: TestService) {
    }

    ngOnInit() {
        this.testService.getTest(0).then((data) => {
            this.test = data;
            console.log(data);
        })
    }
}
