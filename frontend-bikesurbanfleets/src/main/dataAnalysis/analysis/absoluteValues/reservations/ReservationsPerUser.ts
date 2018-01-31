import { HistoryReader } from '../../../../util';
import { HistoryEntitiesJson } from '../../../../../shared/history';
import { Observer } from '../../ObserverPattern';
import  { User, Reservation } from '../../../systemDataTypes/Entities';
import { EnumType } from "typescript";

export class ReservationsPerUser implements Observer {
    private users: Array<User>;
    private bikeFailedReservationsPerUser: Map<number, number>; 
    private slotFailedReservationsPerUser: Map<number, number>;
    private bikeSuccessfulReservationsPerUser: Map<number, number>;
    private slotSuccessfulReservationsPerUser: Map<number, number>;
    
    public constructor() {
        this.bikeFailedReservationsPerUser = new Map<number, number>(); 
        this.slotFailedReservationsPerUser = new Map<number, number>();
        this.bikeSuccessfulReservationsPerUser = new Map<number, number>();
        this.slotSuccessfulReservationsPerUser = new Map<number, number>();
    }
    
    public async init(path: string): Promise<void> {
        try {
            let history: HistoryReader = await HistoryReader.create(path);
            let entities: HistoryEntitiesJson = await history.getEntities("users");
            this.users = entities.instances;
              
            for(let user of this.users) {
                this.bikeFailedReservationsPerUser.set(user.id, 0);
                this.slotFailedReservationsPerUser.set(user.id, 0);
                this.bikeSuccessfulReservationsPerUser.set(user.id, 0);
                this.slotSuccessfulReservationsPerUser.set(user.id, 0);
            }
        }
        catch(error) {
            console.log(error);
        }
        return;
    }
   
    public static async create(path: string): Promise<ReservationsPerUser> {
        let reservationValues = new ReservationsPerUser();
        try {
            await reservationValues.init(path);
        }
        catch {
            console.log('error creating reservations per user data');
        }
        return reservationValues;
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
        
        if (reservation.type === 'BIKE' && reservation.state === 'FAILED') {
            value = this.bikeFailedReservationsPerUser.get(key);
            if (value !== undefined) {
                this.bikeFailedReservationsPerUser.set(key, ++value);
            }
        }
        else if (reservation.type === 'SLOT' && reservation.state === 'FAILED') {
            value = this.slotFailedReservationsPerUser.get(key);
            if (value !== undefined) {
                this.slotFailedReservationsPerUser.set(key, ++value);
            }
        }
        else if (reservation.type === 'BIKE' && reservation.state === 'SUCCESSFUL') {
            value = this.bikeSuccessfulReservationsPerUser.get(key);
            if (value !== undefined) {   
                this.bikeSuccessfulReservationsPerUser.set(key, ++value);
            }
        }
        else if (reservation.type === 'SLOT' && reservation.state === 'SUCCESSFUL') {
            value = this.slotSuccessfulReservationsPerUser.get(key);
            if (value !== undefined) {             
                this.slotSuccessfulReservationsPerUser.set(key, ++value);
            }
        }

    }
  
  public print(): void {
    this.bikeFailedReservationsPerUser.forEach( (value, key) => console.log('User', key,'Bike failed reservations', value));
    this.bikeSuccessfulReservationsPerUser.forEach( (value, key) => console.log('User', key,'Bike successful reservations', value));
    this.slotFailedReservationsPerUser.forEach( (value, key) => console.log('User', key,'Slotfailed reservations', value));
    this.slotSuccessfulReservationsPerUser.forEach( (value, key) => console.log('User', key,'Slot successful reservations', value));
  }
  

               
}