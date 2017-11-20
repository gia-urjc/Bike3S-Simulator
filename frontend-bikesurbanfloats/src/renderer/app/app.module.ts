import { NgModule } from '@angular/core';
import { BrowserModule }  from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';

import { AppComponent } from './app.component';
import { IpcUtilRenderer } from '../util/IpcUtilRenderer';
import { TestService } from './services/TestService'

import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { NgFontAwesomeModule } from '../ng-font-awesome/ngfa.module';
import { LeafletModule } from '@asymmetrik/ngx-leaflet';

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
    providers: [IpcUtilRenderer, TestService]
})
export class AppModule {

}
