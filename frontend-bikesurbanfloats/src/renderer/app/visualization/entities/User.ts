import { DivIcon, Icon, Polyline } from 'leaflet';
import { Geo } from '../../../../shared/util';
import { LeafletUtil } from '../util';
import { Visualization } from '../visualization.component';
import { Bike } from './Bike';
import { Entity, Historic } from './Entity';
import { Station } from './Station';

import './user.css';

function makeIcon(user: User) {
    const icon = new DivIcon({
        className: 'user-marker',
        html: `
            <div>
                <span class="badge badge-pill badge-${user.bike ? 'danger' : 'primary'} user-id">${user.id}</span>
                <span class="fa fa-${user.bike ? 'bicycle' : 'male'} fa-fw user-icon"></span>
            </div>
        `
    });
    Reflect.defineMetadata(Icon, icon, user);
    return icon;
}

@Historic<User>({
    jsonIdentifier: 'users',
    show: {
        at: {
            route: (user) => user.route,
            speed: (user) => user.bike ? user.cyclingVelocity : user.walkingVelocity,
        },
        when: (user) => Boolean(user.position),
        icon: (user) => Reflect.getOwnMetadata(Icon, user) || makeIcon(user),
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
        bike: makeIcon,
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
