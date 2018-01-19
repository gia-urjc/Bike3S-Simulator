import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BrowserModule }  from '@angular/platform-browser';

import { LeafletModule } from '@asymmetrik/ngx-leaflet';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { NgFontAwesomeModule } from 'ng-font-awesome';

import { ElectronAjax } from '../ajax/ElectronAjax';
import { AppComponent } from './app.component';
import { MapComponent } from './map/map.component';
import { Visualization } from './visualization/visualization.component';
import {SchemaformComponent} from './schemaform/schemaform.component';
import {Bootstrap4FrameworkModule, JsonSchemaFormModule} from 'angular2-json-schema-form';

@NgModule({
    imports: [
        NgbModule.forRoot(),
        BrowserModule,
        FormsModule,
        ReactiveFormsModule,
        NgFontAwesomeModule,
        LeafletModule.forRoot(),
        JsonSchemaFormModule.forRoot(Bootstrap4FrameworkModule)
    ],
    declarations: [
        AppComponent,
        MapComponent,
        Visualization,
        SchemaformComponent
    ],
    bootstrap: [ AppComponent ],
    providers: [{
        provide: 'AjaxProtocol',
        useClass: ElectronAjax
    }]
})
export class AppModule {}
