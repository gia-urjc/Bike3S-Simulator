import { HistoryReader } from '../../../../util';
import { HistoryEntitiesJson } from '../../../../../shared/history';
import  { Station, Reservation } from '../../../systemDataTypes/Entities';
import { AbsoluteValue } from '../AbsoluteValue';
import { Info } from "../Info";

export class ReservationsPerStation implements Info {
    private stations: Array<Station>;
    
    public constructor() {
        super('STATION');
    }
    
    public async init(path: string): Promise<void> {
        try {
            let history: HistoryReader = await HistoryReader.create(path);
            let entities: HistoryEntitiesJson = await history.getEntities("stations");
            this.stations = <Station[]> entities.instances;
            
            this.initData(this.stations);
        }
        catch(error) {
            throw new Error('Error accessing to stations: '+error);
        }
        return;
    }
   
    public static async create(path: string): Promise<ReservationsPerStation> {
        let reservationValues = new ReservationsPerStation();
        try {
            await reservationValues.init(path);
        }
        catch(error) {
            throw new Error('Error initializig station data of requested data: '+error);
        }
        return reservationValues;
    }
    
    public update(reservation: Reservation): void {
        let key: number = reservation.station.id;
        
        switch (reservation.type) { 
            case 'BIKE': { 
                if (reservation.state === 'FAILED') {
                    this.increaseFailedBikeReservations(key);
                }
                else {
                    this.increaseSuccessfulBikeReservations(key);
                }
                break;
            }
                
            case 'SLOT': { 
                if (reservation.state === 'FAILED') {
                    this.increaseFailedSlotReservations(key);
                }
                else {
                    this.increaseSuccessfulSlotReservations(key);
                }
                break;
            }
                
                default: throw new Error('Reservation type not identified');
                        
        }
    }
              
}
