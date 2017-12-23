import { JsonIdentifier } from './decorators';
import { Entity } from './Entity';

interface JsonStation {
    id: number,
}

@JsonIdentifier('stations')
export class Station extends Entity {
    reserved: boolean;

    constructor(json: JsonStation) {
        super(json.id);
    }
}
