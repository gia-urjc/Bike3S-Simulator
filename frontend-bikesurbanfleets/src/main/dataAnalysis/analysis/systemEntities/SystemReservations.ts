import { HistoryEntitiesJson } from "../../../../shared/history";
import { HistoryReaderController } from "../../../controllers/HistoryReaderController";
import { Reservation } from "../../systemDataTypes/Entities";

export class SystemReservations {
    private reservations: Array<Reservation>;
    
    public async init(history: HistoryReaderController): Promise<void> {
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
