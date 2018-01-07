import { Entity, Visual } from './Entity';

@Visual<Reservation>({
    jsonIdentifier: 'reservations',
})
export class Reservation extends Entity {
    reserved: boolean;
}
