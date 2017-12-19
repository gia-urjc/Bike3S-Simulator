import { JsonIdentifier } from './decorators';
import { Entity } from './Entity';

interface JsonBike {
    id: number,
}

@JsonIdentifier('bikes')
export class Bike extends Entity {
    reserved: boolean;

    constructor(json: JsonBike) {
        super(json.id);
    }
}
