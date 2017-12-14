import { EntitiesJson } from '../../../../shared/generated/EntitiesJson';
import { ArrayType, Extract, GeoPoint, Route } from '../../../../shared/util';
import { Bike } from './Bike';
import { Entity, VisualEntity } from './Entity';

type JsonUser = ArrayType<Extract<EntitiesJson, 'users'>>

@VisualEntity<JsonUser>({
    fromJson: 'users'
})
export class User extends Entity {
    private $type: string;
    private $walkingVelocity: number;
    private $cyclingVelocity: number;

    position: GeoPoint | null;
    route: Route | null;
    bike: Bike | null;
    // destinationStation

    constructor(json: JsonUser) {
        super(json.id);
        this.$type = json.type;
        this.$walkingVelocity = json.walkingVelocity;
        this.$cyclingVelocity = json.cyclingVelocity;
    }

    get type() {
        return this.$type;
    }

    get walkingVelocity() {
        return this.$walkingVelocity;
    }

    get cyclingVelocity() {
        return this.$cyclingVelocity;
    }
}
