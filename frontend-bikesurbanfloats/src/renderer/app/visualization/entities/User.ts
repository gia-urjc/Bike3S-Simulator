import { DivIcon, Icon, Polyline } from 'leaflet';
import { ReservationState } from '../../../../shared/history';
import { Geo } from '../../../../shared/util';
import { LeafletUtil } from '../util';
import { Visualization } from '../visualization.component';
import { Bike } from './Bike';
import { Entity, Historic } from './Entity';
import { Reservation } from './Reservation';
import { Station } from './Station';

import './user.css';

function updateIcon(user: User) {
    let reservationBadge = '';
    let margin = -250;

    if (user.reservations.find((reservation) => reservation.state === ReservationState.ACTIVE)) {
        reservationBadge = `<span class="badge badge-pill badge-info">R</span>`;
        margin = -350;
    }

    const icon = new DivIcon({
        className: 'user-marker',
        html: `
            <div style="margin-top: ${margin}%">
                ${reservationBadge}
                <span class="badge badge-pill badge-${user.bike ? 'danger' : 'primary'}">${user.id}</span>
                <span class="fa fa-${user.bike ? 'bicycle' : 'male'} fa-fw user-icon"></span>
            </div>
        `
    });
    Reflect.defineMetadata(Icon, icon, user);
}

@Historic<User>({
    jsonIdentifier: 'users',
    marker: {
        at: {
            route: (user) => user.route,
            speed: (user) => user.bike ? user.cyclingVelocity : user.walkingVelocity,
        },
        when: (user) => Boolean(user.position),
        icon: (user) => Reflect.getOwnMetadata(Icon, user),
        on: {
            click: (user) => console.log(user),
            mouseover: (user) => user.route && Visualization.addLayer(Reflect.getOwnMetadata(Polyline, user)),
            mouseout: (user) => Visualization.deleteLayer(Reflect.getOwnMetadata(Polyline, user)),
        },
    },
    on: {
        init: (user) => {
            updateIcon(user);
        },
        propertyUpdate: {
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
