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
import {SchemaFormComponent} from './schemaform-component/schemaform.component';
import { Bootstrap4FrameworkModule } from 'angular6-json-schema-form';
import {AppRoutingModule} from "./app.routes";
import {SimulateComponent} from "./simulate-component/simulate.component";
import {SimulatecoreComponent} from "./simulate-core-component/simulatecore.component";
import {SimulateusergenComponent} from "./simulate-usergen-component/simulateusergen.component";
import {SimulationspinnerComponent} from "./simulation-spinner-component/simulationspinner.component";
import {ConfigurationComponent} from "./configuration-component/configuration.component";
import {LeafletDrawModule} from "@asymmetrik/ngx-leaflet-draw";
import {JsonTreeViewComponent} from "./jsoneditor-component/jsoneditor.component";
import {ConfigurationSaveComponent} from "./configuration-save-component/configurationsave.component";
import { AnalyseHistoryComponent } from './analyse-history-component/analysehistory.component';
import {FlexLayoutModule} from "@angular/flex-layout";
import { ConfigurationLoadComponent } from './configuration-load-globalconfig/configuration-load.component';
import { SchemaFormGlobalComponent } from './schemaform-global-component/schemaform-global.component';
import { ConfDownMapComponent } from './configuration-download-map/configuration-download-map.component';

@NgModule({
    imports: [
        NgbModule.forRoot(),
        BrowserModule,
        FormsModule,
        ReactiveFormsModule,
        NgFontAwesomeModule,
        LeafletModule.forRoot(),
        LeafletDrawModule.forRoot(),
        Bootstrap4FrameworkModule,
        AppRoutingModule,
        NgbModalModule,
        NgbProgressbarModule,
        FlexLayoutModule,
    ],
    declarations: [
        AppComponent,
        MapComponent,
        Visualization,
        SchemaFormComponent,
        SchemaFormGlobalComponent,
        MenuComponent,
        SimulateComponent,
        SimulatecoreComponent,
        SimulateusergenComponent,
        SimulationspinnerComponent,
        ConfigurationComponent,
        JsonTreeViewComponent,
        ConfigurationSaveComponent,
        AnalyseHistoryComponent,
        ConfigurationLoadComponent,
        ConfDownMapComponent
    ],
    bootstrap: [AppComponent],
    providers: [{
        provide: 'AjaxProtocol',
        useClass: ElectronAjax
    }, NgbModal],
    entryComponents: [
        ConfigurationSaveComponent,
        ConfigurationLoadComponent,
        ConfDownMapComponent
    ]
})
export class AppModule {}
