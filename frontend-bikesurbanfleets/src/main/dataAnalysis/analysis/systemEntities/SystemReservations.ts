import { HistoryEntitiesJson } from "../../../../shared/history";
import { HistoryReader } from "../../../util";
import { Reservation } from "../../systemDataTypes/Entities";

export class SystemReservations {
    private reservations: Array<Reservation>;
    
    public async init(history: HistoryReader): Promise<void> {
        try {
            let reservationEntities: HistoryEntitiesJson = await history.getEntities('reservations');
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
