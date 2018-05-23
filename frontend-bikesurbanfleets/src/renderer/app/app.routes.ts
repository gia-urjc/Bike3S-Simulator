import {RouterModule, Routes} from "@angular/router";
import {Visualization} from "./visualization-component/visualization.component";
import {MenuComponent} from "./menu-component/menu.component";
import {NgModule} from "@angular/core";
import {SimulateComponent} from "./simulate-component/simulate.component";
import {ConfigurationComponent} from "./configuration-component/configuration.component";
import { AnalyseHistoryComponent } from "./analyse-history-component/analysehistory.component";

export const routes:Routes = [
    {
        path: 'menu',
        component: MenuComponent
    },
    {
        path: 'visualization',
        component: Visualization
    },
    {
        path: 'simulate',
        component: SimulateComponent
    },
    {
        path: 'configuration',
        component: ConfigurationComponent
    },
    {
        path: 'analyse',
        component: AnalyseHistoryComponent,
    }
];

@NgModule({
    imports: [RouterModule.forRoot(routes, {useHash: true})],
    exports: [RouterModule]
})
export class AppRoutingModule {
}