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
    }
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
