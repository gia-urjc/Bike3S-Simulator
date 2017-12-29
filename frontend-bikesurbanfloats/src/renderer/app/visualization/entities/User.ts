import { GeoPoint, Route } from '../../../../shared/util';
import { Bike } from './Bike';
import { JsonIdentifier, VisualEntity } from './decorators';
import { Entity } from './Entity';
import { Station } from './Station';

@JsonIdentifier('users')
@VisualEntity({
    showAt: (user: User) => user.position,
    move: {
        route: (user: User) => user.route,
        speed: (user: User) => user.bike === null ? user.walkingVelocity : user.cyclingVelocity,
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
