import { HistoryReader } from '../../../../util';
import { HistoryEntitiesJson } from '../../../../../shared/history';
import { Observer } from '../../ObserverPattern';
import  { User, Reservation } from '../../../systemDataTypes/Entities';
import { AbsoluteValue } from '../AbsoluteValue';
import { Data } from '../Data';
import { EnumType } from "typescript";

export class ReservationsPerUser implements Data {
    private users: Array<User>;
    private bikeFailedReservationsPerUser: Map<number, AbsoluteValue>; 
    private slotFailedReservationsPerUser: Map<number, AbsoluteValue>;
    private bikeSuccessfulReservationsPerUser: Map<number, AbsoluteValue>;
    private slotSuccessfulReservationsPerUser: Map<number, AbsoluteValue>;
    
    public constructor() {
        this.bikeFailedReservationsPerUser = new Map<number, AbsoluteValue>(); 
        this.slotFailedReservationsPerUser = new Map<number, AbsoluteValue>();
        this.bikeSuccessfulReservationsPerUser = new Map<number, AbsoluteValue>();
        this.slotSuccessfulReservationsPerUser = new Map<number, AbsoluteValue>();
    }
    
    public async init(path: string): Promise<void> {
        try {
            let history: HistoryReader = await HistoryReader.create(path);
            let entities: HistoryEntitiesJson = await history.getEntities("users");
            this.users = entities.instances;
              
            for(let user of this.users) {
                this.bikeFailedReservationsPerUser.set(user.id, { name: "Failed bike reservations", value: 0});
                this.slotFailedReservationsPerUser.set(user.id, { name: "Failed slot reservations", value: 0});
                this.bikeSuccessfulReservationsPerUser.set(user.id, { name: "Successful bike reservations", value: 0});
                this.slotSuccessfulReservationsPerUser.set(user.id, { name: "Successful slot reservations", value: 0});
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
     let absoluteValue: AbsoluteValue = this.bikeFailedReservationsPerUser.get(userId);
        return absoluteValue.value;
    }

    public getSlotFailedReservationsOfUser(userId: number): number | undefined {
        let absoluteValue: AbsoluteValue = this.slotFailedReservationsPerUser.get(userId);
        return absoluteValue.value;
    }
    
    public getBikeSuccessfulReservationsOfUser(userId: number): number | undefined {
        let absoluteValue: AbsoluteValue = this.bikeSuccessfulReservationsPerUser.get(userId);
        return absoluteValue.value;
    }
    
    public getSlotSuccessfulReservationsOfUser(userId: number): number | undefined {
        let absoluteValue: AbsoluteValue = this.slotSuccessfulReservationsPerUser.get(userId);
        return absoluteValue.value;
    }
        
    public update(reservation: Reservation): void {
        let key: number = reservation.user.id;
        let absoluteValue: AbsoluteValue | undefined; 
        
        if (reservation.type === 'BIKE' && reservation.state === 'FAILED') {
            absoluteValue = this.bikeFailedReservationsPerUser.get(key);
            if (absoluteValue !== undefined) {
                absoluteValue.value++;
            }
        }
        
        else if (reservation.type === 'SLOT' && reservation.state === 'FAILED') {
            absoluteValue = this.slotFailedReservationsPerUser.get(key);
            if (absoluteValue !== undefined) {
                absoluteValue.value++;
            }
        }
            
        else if (reservation.type === 'BIKE' && reservation.state === 'ACTIVE') {
            absoluteValue = this.bikeSuccessfulReservationsPerUser.get(key);
if (absoluteValue !== undefined) {
                absoluteValue.value++;
            }
        }
            
        else if (reservation.type === 'SLOT' && reservation.state === 'ACTIVE') {
            absoluteValue = this.slotSuccessfulReservationsPerUser.get(key);
if (absoluteValue !== undefined) {
                absoluteValue.value++;
            }
        }

    }
  
  public toString(): string {
      let src: string='';
    this.bikeFailedReservationsPerUser.forEach( (absoluteValue, key) => 
        src+='User: '+key+' '+absoluteValue.name+': '+absoluteValue.value+'\n'));
    this.bikeSuccessfulReservationsPerUser.forEach( (absoluteValue, key) => 
        src+='User: '+key+' '+absoluteValue.name+': '+absoluteValue.value+'\n')); 
        src+='User: '+key+' Successful bike reservations: '+value+'\n'));
    this.slotFailedReservationsPerUser.forEach( (absoluteValue, key) => 
        src+='User: '+key+' '+absoluteValue.name+': '+absoluteValue.value+'\n'));
    this.slotSuccessfulReservationsPerUser.forEach( (absoluteValue, key) => 
        src+='User: '+key+' '+absoluteValue.name+': '+absoluteValue.value+'\n'));
  }
}