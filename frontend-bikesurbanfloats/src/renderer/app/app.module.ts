import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BrowserModule }  from '@angular/platform-browser';

import { LeafletModule } from '@asymmetrik/ngx-leaflet';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { NgFontAwesomeModule } from 'ng-font-awesome';

import { ElectronAjax } from '../ajax/ElectronAjax';
import { AppComponent } from './app.component';

@NgModule({
    imports: [
        NgbModule.forRoot(),
        BrowserModule,
        FormsModule,
        NgFontAwesomeModule,
        LeafletModule.forRoot()
    ],
    declarations: [
        AppComponent
    ],
    bootstrap: [ AppComponent ],
    providers: [{
        provide: 'AjaxProtocol',
        useClass: ElectronAjax
    }]
})
export class AppModule {}
