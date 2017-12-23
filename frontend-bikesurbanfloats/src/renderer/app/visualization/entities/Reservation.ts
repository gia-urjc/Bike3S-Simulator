import { JsonIdentifier } from './decorators';
import { Entity } from './Entity';

interface JsonReservation {
    id: number,
}

@JsonIdentifier('reservations')
export class Reservation extends Entity {
    reserved: boolean;

    constructor(json: JsonReservation) {
        super(json.id);
    }
}
