import {RouterModule, Routes} from "@angular/router";
import {Visualization} from "./visualization-component/visualization.component";
import {MenuComponent} from "./menu-component/menu.component";
import {NgModule} from "@angular/core";

export const routes:Routes = [
    {
        path: 'menu',
        component: MenuComponent
    },
    {
        path: 'visualization',
        component: Visualization
    }
];

@NgModule({
    imports: [RouterModule.forRoot(routes, {useHash: true})],
    exports: [RouterModule]
})
export class AppRoutingModule {
}