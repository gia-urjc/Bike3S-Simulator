import { DivIcon, Polyline } from 'leaflet';
import { Geo } from '../../../../shared/util';
import { LeafletUtil } from '../util';
import { Visualization } from '../visualization.component';
import { Bike } from './Bike';
import { Entity, Visual } from './Entity';
import { Station } from './Station';

@Visual<User>({
    jsonIdentifier: 'users',
    show: {
        at: {
            route: (user) => user.route,
            speed: (user) => user.bike ? user.cyclingVelocity : user.walkingVelocity,
        },
        when: (user) => Boolean(user.position),
        icon: (user) => new DivIcon({
            className: '',
            iconSize: [20, 40],
            html: `
                <div style="background: ${user.bike ? 'red' : 'blue'}; color: white; display: flex; justify-content: center;">
                    ${user.id}
                </div>
            `
        }),
        onMarkerEvent: {
            click: (user) => console.log(user),
            mouseover: (user) => user.route && Visualization.addLayer(Reflect.getOwnMetadata(Polyline, user)),
            mouseout: (user) => Visualization.deleteLayer(Reflect.getOwnMetadata(Polyline, user)),
        },
    },
    onChange: {
        route: (user) => {
            Visualization.deleteLayer(Reflect.getOwnMetadata(Polyline, user));
            if (user.route) {
                Reflect.defineMetadata(Polyline, LeafletUtil.polyline(user.route), user);
            }
        },
        position: (user) => {
            if (!user.position) {
                Visualization.deleteLayer(Reflect.getOwnMetadata(Polyline, user));
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
