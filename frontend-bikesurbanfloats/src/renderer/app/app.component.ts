import { Component, OnInit } from '@angular/core';

@Component({
    selector: 'my-app',
    template: require('./app.component.html'),
    styles: [require('./app.component.css')]
})
export class AppComponent implements OnInit {

    ngOnInit() {
        console.log("Hello from console!");
    }
}
