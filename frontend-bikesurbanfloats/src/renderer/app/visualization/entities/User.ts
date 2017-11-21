import { Visual } from '../util/Visual';
import { Entity } from './Entity';

interface JsonUser {
    id: number,
    type: string,
    walkingVelocity: number,
    cyclingVelocity: number,
}

@Visual({
    fromJson: 'users'
})
export class User extends Entity {

    private $type: string;
    private $walkingVelocity: number;
    private $cyclingVelocity: number;

    constructor(json: JsonUser) {
        super(json.id);
        this.$type = json.type;
        this.$walkingVelocity = json.walkingVelocity;
        this.$cyclingVelocity = json.cyclingVelocity;
    }
}
