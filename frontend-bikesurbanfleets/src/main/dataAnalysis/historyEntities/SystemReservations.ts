
import { HistoryEntitiesJson } from '../../../shared/history';
import { Reservation } from "../systemDataTypes/Entities";
import { HistoryReader } from '../HistoryReader';

export class SystemReservations {
    private reservations: Array<Reservation>;
    
    public init(history: HistoryReader): void {
        try {
            let reservationEntities: HistoryEntitiesJson = history.getEntities('reservations');
            this.reservations = <Reservation[]> reservationEntities.instances;
        }
        catch(error) {
            throw new Error('Error getting reservations: '+error);
        }
    }

    public getReservations(): Array<Reservation> {
        return this.reservations;
    }
    
}
