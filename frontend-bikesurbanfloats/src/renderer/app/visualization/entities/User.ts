import { DivIcon } from 'leaflet';
import { ReservationState } from '../../../../shared/history';
import { Geo } from '../../../../shared/util';
import { LeafletUtil } from '../util';
import { Visualization } from '../visualization.component';
import { Bike } from './Bike';
import { Entity, Historic } from './Entity';
import { Reservation } from './Reservation';
import { Station } from './Station';

import './user.css';

const UserIcon = Symbol('UserIcon');
const UserRoute = Symbol('UserRoute');

function updateIcon(user: User) {
    const maxHeight = 60;

    let reservationBadge = '';

    if (user.reservations.find((reservation) => reservation.state === ReservationState.ACTIVE)) {
        reservationBadge = `<span class="badge badge-pill badge-info">R</span>`;
    }

    const icon = new DivIcon({
        className: 'user-marker',
        iconSize: [20, maxHeight],
        iconAnchor: [10, maxHeight],
        html: `
            <div>
                ${reservationBadge}
                <span class="badge badge-pill badge-${user.bike ? 'danger' : 'primary'}">${user.id}</span>
                <span class="fa fa-fw fa-${user.bike ? 'bicycle' : 'male'}"></span>
            </div>
        `
    });

    Reflect.defineMetadata(UserIcon, icon, user);
}

@Historic<User>({
    jsonIdentifier: 'users',
    marker: {
        at: {
            route: (user) => user.route,
            speed: (user) => user.bike ? user.cyclingVelocity : user.walkingVelocity,
        },
        when: (user) => Boolean(user.position),
        icon: (user) => Reflect.getOwnMetadata(UserIcon, user),
        on: {
            click: (user) => console.log(user),
            mouseover: (user) => user.route && Visualization.addLayer(Reflect.getOwnMetadata(UserRoute, user)),
            mouseout: (user) => Visualization.deleteLayer(Reflect.getOwnMetadata(UserRoute, user)),
        },
    },
    on: {
        init: (user) => {
            updateIcon(user);
        },
        propertyUpdate: {
            route: (user) => {
                Visualization.deleteLayer(Reflect.getOwnMetadata(UserRoute, user));
                if (user.route) {
                    Reflect.defineMetadata(UserRoute, LeafletUtil.polyline(user.route), user);
                }
            },
            position: (user) => {
                if (!user.position) {
                    Visualization.deleteLayer(Reflect.getOwnMetadata(UserRoute, user));
                }
            },
            bike: (user) => {
                updateIcon(user);
            },
            reservations: (user) => {
                updateIcon(user);
            }
        },
        referenceUpdate: {
            reservations: (user, reservation) => {
                updateIcon(user);
            },
        }
    }
})
export class User extends Entity {
    type: string;
    walkingVelocity: number;
    cyclingVelocity: number;
    reservations: Array<Reservation>;

    position: Geo.Point | null;
    route: Geo.Route | null;
    bike: Bike | null;
    destinationStation: Station | null;
}
