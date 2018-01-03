import { DivIcon } from 'leaflet';
import { GeoPoint, Route } from '../../../../shared/util';
import { Bike } from './Bike';
import { JsonIdentifier, VisualEntity } from './decorators';
import { Entity } from './Entity';
import { Station } from './Station';

@JsonIdentifier('users')
@VisualEntity({
    show: {
        when: (user: User) => Boolean(user.position),
        route: (user: User) => user.route,
        speed: (user: User) => user.bike ? user.cyclingVelocity : user.walkingVelocity,
    },
    icon: (user: User) => new DivIcon({
        className: '',
        iconSize: [10, 10],
        html: `
        <div style="background: ${user.bike ? 'red' : 'blue'}; color: white;">
            ${user.id}
        </div>
        `
    })

})
export class User extends Entity {
    type: string;
    walkingVelocity: number;
    cyclingVelocity: number;

    position: GeoPoint | null;
    route: Route | null;
    bike: Bike | null;
    destinationStation: Station | null
}
