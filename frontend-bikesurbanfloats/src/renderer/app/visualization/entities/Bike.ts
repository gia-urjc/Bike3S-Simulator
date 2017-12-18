import { Entity, VisualEntity } from './Entity';

interface JsonBike {
    id: number,
}

@VisualEntity({
    fromJson: 'bikes',
})
export class Bike extends Entity {
    reserved: boolean;

    constructor(json: JsonBike) {
        super(json.id);
    }
}
