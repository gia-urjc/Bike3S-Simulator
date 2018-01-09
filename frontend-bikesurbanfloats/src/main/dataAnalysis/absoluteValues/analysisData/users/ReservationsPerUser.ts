import { HistoryReader } from '../../../../util';
import { HistoryEntitiesJson } from '../../../../../shared/History';
import { Observer } from '../../ObserverPattern';
import  { User, Reservation, ReservationType, ReservationState } from '../../../systemDataTypes/Entities';

export class ReservationsPerUser implements Observer {
    private users: Array<User>;
    private bikeFailedReservationsPerUser: Map<number, number>; 
    private slotFailedReservationsPerUser: Map<number, number>;
    private bikeSuccessfulReservationsPerUser: Map<number, number>;
    private slotSuccessfulReservationsPerUser: Map<number, number>;
    
    private constructor() {
        this.bikeFailedReservationsPerUser = new Map<number, number>(); 
        this.slotFailedReservationsPerUser = new Map<number, number>();
        this.slotFailedReservationsPerUser = new Map<number, number>();
        this.slotSuccessfulReservationsPerUser = new Map<number, number>();
    }
    
    private async init(path: string): Promise<void> {
        let history: HistoryReader = await HistoryReader.create(path);
        try {
        let entities: HistoryEntitiesJson = await history.getEntities("users");
        this.users = entities.instances;   
        
        for(let user of this.users) {
            console.log('user ', user.id, ' ' );
            this.bikeFailedReservationsPerUser.set(user.id, 0);
            this.slotFailedReservationsPerUser.set(user.id, 0);            
            this.bikeSuccessfulReservationsPerUser.set(user.id, 0);
            this.slotSuccessfulReservationsPerUser.set(user.id, 0);
        }
        }
        catch(error) {
            console.log('error getting user:', error);
        }
        return;
    }
   
    public static async create(path: string): Promise<ReservationsPerUser> {
        let reservationValues = new ReservationsPerUser();
        try {
            await reservationValues.init(path);
            return reservationValues;
        }
        catch {
            console.log('error initializing reservations per user data');
        }
        return;
    }
    
    public getBikeFailedReservationsOfUser(userId: number): number| undefined {
     return this.bikeFailedReservationsPerUser.get(userId);
    }

    public getSlotFailedReservationsOfUser(userId: number): number | undefined {
        return this.slotFailedReservationsPerUser.get(userId);
    }
    
    public getBikeSuccessfulReservationsOfUser(userId: number): number | undefined {
        return this.bikeSuccessfulReservationsPerUser.get(userId);
    }
    
    public getSlotSuccessfulReservationsOfUser(userId: number): number | undefined {
        return this.slotSuccessfulReservationsPerUser.get(userId);
    }
        
    public update(reservation: Reservation): void {
        let key: number = reservation.user.id;
        let value: number | undefined;
        
        if (reservation.type === ReservationType.BIKE && reservation.state === ReservationState.FAILED) {
            value = this.bikeFailedReservationsPerUser.get(key);
            if (value !== undefined) {
                this.bikeFailedReservationsPerUser.set(key, ++value);
                console.log('(', key, ' ', value, ') ')
            }
        }
        else if (reservation.type === ReservationType.SLOT && reservation.state === ReservationState.FAILED) {
            value = this.slotFailedReservationsPerUser.get(key);
            if (value !== undefined) {
                console.log('(', key, ' ', value, ')')                 
                this.slotFailedReservationsPerUser.set(key, ++value);
            }
        }
        else if (reservation.type === ReservationType.BIKE && reservation.state === ReservationState.SUCCESSFUL) {
            value = this.bikeSuccessfulReservationsPerUser.get(key);
            if (value !== undefined) {
                console.log('(', key, ' ', value, ')')
                this.bikeSuccessfulReservationsPerUser.set(key, ++value);
            }
        }
        else if (reservation.type === ReservationType.SLOT && reservation.state === ReservationState.SUCCESSFUL) {
            value = this.slotSuccessfulReservationsPerUser.get(key);
            if (value !== undefined) {
                console.log('(', key, ' ', value, ')')                                
                this.slotSuccessfulReservationsPerUser.set(key, ++value);
            }
        }
        console.log('updated');

    }

               
}