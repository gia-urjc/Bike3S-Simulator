import { EntitiesJson } from '../../../../shared/generated/EntitiesJson';
import { ArrayType, Extract } from '../../../../shared/util';
import { Entity, VisualEntity } from './Entity';

type JsonBike = ArrayType<Extract<EntitiesJson, 'bikes'>>;

@VisualEntity<JsonBike>({
    fromJson: 'bikes',
})
export class Bike extends Entity {
    constructor(json: JsonBike) {
        super(json.id);
    }
}
