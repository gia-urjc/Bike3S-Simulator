import { HistoryReader } from '../../../../util';
import { HistoryEntitiesJson } from '../../../../../shared/history';
import  { User, Reservation } from '../../../systemDataTypes/Entities';
import { ReservationsInfo } from './ReservationsInfo';

export class ReservationsPerUser extends ReservationsInfo {
    private users: Array<User>;
    
    public constructor() {
        super('USER');
    }
    
    public async init(path: string): Promise<void> {
        try {
            let history: HistoryReader = await HistoryReader.create(path);
            let entities: HistoryEntitiesJson = await history.getEntities("users");
            this.users = entities.instances;
            
            this.initData(this.users);
        }
              
        catch(error) {
            throw new Error('Error accessing to users: '+error);
        }
        return;
    }
   
    public static async create(path: string): Promise<ReservationsPerUser> {
        let reservationValues = new ReservationsPerUser();
        try {
            await reservationValues.init(path);
        }
        catch(error) {
            throw new Error('Error initializing users of requested data'+error);
        }
        return reservationValues;
    }
    
    public update(reservation: Reservation): void {
        let key: number = reservation.user.id;
        
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
        
        
