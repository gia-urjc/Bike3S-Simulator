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
var ruteshown= false;

function updateIcon(user: User) {
    const maxHeight = 60;

    let reservationBadge = '';

    if ((user.reservation && user.reservation.state === ReservationState.ACTIVE) || user.hasreservation) {
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
            route: (user) => user.decodedroute,
            speed: (user) => (user.bike || user.hasbike) ? user.cyclingVelocity : user.walkingVelocity,
        },
        when: (user) => Boolean(user.position),
        icon: (user) => Reflect.getOwnMetadata(UserIcon, user),
        on: {
            click: (user) => console.log(user),
            mouseover: (user) => user.decodedroute && Visualization.addLayer(Reflect.getOwnMetadata(UserRoute, user)),
            mouseout: (user) => Visualization.deleteLayer(Reflect.getOwnMetadata(UserRoute, user)),
        },
        popup: (user) => {
        if (user.bike) {
            return `
            <div><strong>User #${user.id}</strong></div>
            <div>Has bike: #${user.bike.id}</div>`
            } else if (user.hasbike) {
            return `
            <div> <strong>User #${user.id}</strong></div>
            <div>Has bike: yes</div>`
            } else {
            return `
            <div> <strong>User #${user.id}</strong></div>
            <div>Has bike: no</div>`
            }
        },
    },
    on: {
        delete: (user) => {
            Visualization.deleteLayer(Reflect.getOwnMetadata(UserRoute, user));
        },
        init: (user) => {
            user.decodedroute = Geo.decodeRoute(user.route);
            if (user.decodedroute) {
                Reflect.defineMetadata(UserRoute, LeafletUtil.polyline(user.decodedroute), user);
            }
            updateIcon(user);
        },
        propertyUpdate: {
            route: (user) => {
                Visualization.deleteLayer(Reflect.getOwnMetadata(UserRoute, user));
                user.decodedroute = Geo.decodeRoute(user.route);
                if (user.decodedroute) {
                    Reflect.defineMetadata(UserRoute, LeafletUtil.polyline(user.decodedroute), user);
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
            hasbike: (user) => {
                updateIcon(user);
            },
            hasreservation: (user) => {
                updateIcon(user);
            },
            reservation: (user) => {
                updateIcon(user);
            },
        },
        referenceUpdate: {
            reservation: (user, reservation) => {
                updateIcon(user);
            },
        }
/*        referenceUpdate: {
            bike: (user, bike) => {
                updateIcon(user);
            },
*/        }
    
})
export class User extends Entity {
    type: string;
    walkingVelocity: number;
    cyclingVelocity: number;
//the reservations and bike references are not passed in the history, instead, hasbike and hasreservation is passed
    reservation: Reservation;
    bike: Bike | null;
        
    hasbike: boolean;    
    hasreservation: boolean;    

    position: Geo.Point | null;
    route: Geo.EncodedRoute | null;
    decodedroute: Geo.Route | null;
    //the destination station is also not passed
    destinationStation: Station | null;
}
