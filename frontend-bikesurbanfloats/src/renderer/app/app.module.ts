import { NgModule } from '@angular/core';
import { BrowserModule }  from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';

import { AppComponent } from './app.component';

import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { NgFontAwesomeModule } from '../ng-font-awesome/ngfa.module';
import { LeafletModule } from '@asymmetrik/ngx-leaflet';

import  {MainCommunicator} from '../app/communicator/MainCommunicator';
import  {TestService} from '../app/services/TestService';

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
    providers: [ MainCommunicator, TestService]
})
export class AppModule {

}
