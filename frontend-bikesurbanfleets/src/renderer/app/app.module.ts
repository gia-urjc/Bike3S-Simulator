import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BrowserModule }  from '@angular/platform-browser';

import { LeafletModule } from '@asymmetrik/ngx-leaflet';
import {NgbModal, NgbModalModule, NgbModule, NgbProgressbarModule} from '@ng-bootstrap/ng-bootstrap';
import { NgFontAwesomeModule } from 'ng-font-awesome';

import { ElectronAjax } from '../ajax/ElectronAjax';
import { AppComponent } from './app.component';
import { MapComponent } from './map/map.component';
import { Visualization } from './visualization-component/visualization.component';
import { MenuComponent } from './menu-component/menu.component';
import {SchemaformComponent} from './schemaform-component/schemaform.component';
import {Bootstrap4FrameworkModule, JsonSchemaFormModule} from 'angular2-json-schema-form';
import {AppRoutingModule} from "./app.routes";
import {SimulateComponent} from "./simulate-component/simulate.component";
import {SimulatecoreComponent} from "./simulate-core-component/simulatecore.component";
import {SimulateusergenComponent} from "./simulate-usergen-component/simulateusergen.component";
import {SimulationspinnerComponent} from "./simulation-spinner-component/simulationspinner.component";

@NgModule({
    imports: [
        NgbModule.forRoot(),
        BrowserModule,
        FormsModule,
        ReactiveFormsModule,
        NgFontAwesomeModule,
        LeafletModule.forRoot(),
        Bootstrap4FrameworkModule,
        JsonSchemaFormModule.forRoot(Bootstrap4FrameworkModule),
        AppRoutingModule,
        NgbModalModule,
        NgbProgressbarModule,
    ],
    declarations: [
        AppComponent,
        MapComponent,
        Visualization,
        SchemaformComponent,
        MenuComponent,
        SimulateComponent,
        SimulatecoreComponent,
        SimulateusergenComponent,
        SimulationspinnerComponent
    ],
    bootstrap: [AppComponent],
    providers: [{
        provide: 'AjaxProtocol',
        useClass: ElectronAjax
    }, NgbModal]
})
export class AppModule {}
