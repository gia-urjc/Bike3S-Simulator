import { ReservationState, ReservationType, Timestamp } from '../../../../shared/history';
import { Bike } from './Bike';
import { Entity, Historic } from './Entity';
import { Station } from './Station';
import { User } from './User';

@Historic<Reservation>({
    jsonIdentifier: 'reservations',
})
export class Reservation extends Entity {
    startTime: Timestamp;
    endTime: Timestamp | null;
    type: ReservationType;
    state: ReservationState;
    user: User;
    station: Station;
    bike: Bike | null;
}
