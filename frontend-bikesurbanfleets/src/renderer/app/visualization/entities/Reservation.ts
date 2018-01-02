import { JsonIdentifier } from './decorators';
import { Entity } from './Entity';

@JsonIdentifier('reservations')
export class Reservation extends Entity {
    reserved: boolean;
}
