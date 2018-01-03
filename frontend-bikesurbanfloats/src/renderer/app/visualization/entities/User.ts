import { DivIcon, Polyline } from 'leaflet';
import { Geo } from '../../../../shared/util';
import { LeafletUtil } from '../util';
import { VisualizationComponent } from '../visualization.component';
import { Bike } from './Bike';
import { JsonIdentifier, VisualEntity } from './decorators';
import { Entity } from './Entity';
import { Station } from './Station';

@JsonIdentifier('users')
@VisualEntity<User>({
    show: {
        when: (user) => Boolean(user.position),
        route: (user) => user.route,
        speed: (user) => user.bike ? user.cyclingVelocity : user.walkingVelocity,
    },
    icon: (user) => new DivIcon({
        className: '',
        iconSize: [20, 40],
        html: `
        <div style="background: ${user.bike ? 'red' : 'blue'}; color: white; display: flex; justify-content: center;">
            ${user.id}
        </div>
        `
    }),
    onAction: {
        click: (user) => console.log(user),
        mouseover: (user) => user.route && VisualizationComponent.addLayer(Reflect.getOwnMetadata(Polyline, user)),
        mouseout: (user) => VisualizationComponent.deleteLayer(Reflect.getOwnMetadata(Polyline, user)),
    },
    onChange: {
        route: (user) => {
            VisualizationComponent.deleteLayer(Reflect.getOwnMetadata(Polyline, user));
            if (user.route) {
                Reflect.defineMetadata(Polyline, LeafletUtil.polyline(user.route), user);
            }
        },
        position: (user) => {
            if (!user.position) {
                VisualizationComponent.deleteLayer(Reflect.getOwnMetadata(Polyline, user));
            }
        },
    }
})
export class User extends Entity {
    type: string;
    walkingVelocity: number;
    cyclingVelocity: number;

    position: Geo.Point | null;
    route: Geo.Route | null;
    bike: Bike | null;
    destinationStation: Station | null;
}
