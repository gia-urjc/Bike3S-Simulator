import { GeoPoint, Route } from '../../../../shared/util';
import { Bike } from './Bike';
import { Entity, VisualEntity } from './Entity';

interface JsonUser {
    id: number,
    type: string,
    walkingVelocity: number,
    cyclingVelocity: number,
}

@VisualEntity({
    fromJson: 'users'
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
