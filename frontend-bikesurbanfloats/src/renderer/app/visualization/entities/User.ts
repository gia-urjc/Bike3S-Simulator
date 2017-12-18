import { HistoryEntity } from '../../../../shared/history';
import { GeoPoint, Route } from '../../../../shared/util';
import { Bike } from './Bike';
import { JsonIdentifier, VisualEntity } from './decorators';
import { Entity } from './Entity';

interface JsonUser extends HistoryEntity {
    type: string,
    walkingVelocity: number,
    cyclingVelocity: number,
}

@JsonIdentifier('users')
@VisualEntity({
    show: (user: User) => user.position !== null,
    moveAlong: (user: User) => user.route,
    speed: (user: User) => user.bike === null ? user.walkingVelocity : user.cyclingVelocity,
})
export class User extends Entity {
    type: string;
    walkingVelocity: number;
    cyclingVelocity: number;

    position: GeoPoint | null;
    route: Route | null;
    bike: Bike | null;
    // destinationStation

    constructor(json: JsonUser) {
        super(json.id);
        this.type = json.type;
        this.walkingVelocity = json.walkingVelocity;
        this.cyclingVelocity = json.cyclingVelocity;
    }
}
