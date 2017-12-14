import { JsonObject } from '../../../../shared/util';
import { Entity, VisualEntity } from './Entity';

interface JsonBike extends JsonObject {
    id: number,
}

@VisualEntity<JsonBike>({
    fromJson: 'bikes',
})
export class Bike extends Entity {
    constructor(json: JsonBike) {
        super(json.id);
    }
}
