import { HistoryReader } from '../../../../../../util';
import { HistoryEntitiesJson } from '../../../../../../../shared/history';
import { Reservation } from '../../../../../systemDataTypes/Entities';

export class SystemReservationsInfo {
    private reservations: Array<Reservation>;

    public async init(path: string): Promise<void> {
        try {
            let history: HistoryReader = await HistoryReader.create(path);
            let entities: HistoryEntitiesJson = await history.getEntities('reservations');
            this.reservations = <Reservation[]> entities.instances;
        }
        catch(error) {
            throw new Error('Error accessing to reservations: '+error);
        }
    }

    public getReservations(): Array<Reservation> {
        return this.reservations; 
    }
}
