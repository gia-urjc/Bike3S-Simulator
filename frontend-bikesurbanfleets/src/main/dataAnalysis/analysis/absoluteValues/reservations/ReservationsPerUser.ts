import { HistoryReader } from '../../../../util';
import { HistoryEntitiesJson } from '../../../../../shared/history';
import  { User, Reservation } from '../../../systemDataTypes/Entities';
import { AbsoluteValue } from '../AbsoluteValue';
import { Data } from '../Data';

export class ReservationsPerUser implements Data {
    private users: Array<User>;
    private bikeFailedReservationsPerUser: Map<number, AbsoluteValue>; 
    private slotFailedReservationsPerUser: Map<number, AbsoluteValue>;
    private bikeSuccessfulReservationsPerUser: Map<number, AbsoluteValue>;
    private slotSuccessfulReservationsPerUser: Map<number, AbsoluteValue>;
  private factType: string;
  private entityType: string;
    
    public constructor() {
        this.factType = "RESERVATION";
        this.entityType= "USER";
        this.bikeFailedReservationsPerUser = new Map<number, AbsoluteValue>(); 
        this.slotFailedReservationsPerUser = new Map<number, AbsoluteValue>();
        this.bikeSuccessfulReservationsPerUser = new Map<number, AbsoluteValue>();
        this.slotSuccessfulReservationsPerUser = new Map<number, AbsoluteValue>();
    }
  
  public getFactType(): string {
      return this.factType;
    }
  
    public getEntityType(): string {
      return this.entityType;
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
  
  private increaseValue(data: Map<number, AbsoluteValue>, key: number): void { 
      let absoluteValue: AbsoluteValue = data.get(key);
      if (absoluteValue !== undefined) {  // a gotten map value could be undefined
          absoluteValue.value++;
      }
  }
        
    public update(reservation: Reservation): void {
        let key: number = reservation.user.id;
        let absoluteValue: AbsoluteValue | undefined; 
        
        if (reservation.type === 'BIKE' && reservation.state === 'FAILED') {
            this.increaseValue(this.bikeFailedReservationsPerUser, key);
        }
        
        else if (reservation.type === 'SLOT' && reservation.state === 'FAILED') {
            this.increaseValue(this.slotFailedReservationsPerUser, key);
        }
            
        else if (reservation.type === 'BIKE' && reservation.state === 'ACTIVE') {
            this.increaseValue(this.bikeSuccessfulReservationsPerUser, key);
        }
            
        else if (reservation.type === 'SLOT' && reservation.state === 'ACTIVE') {
            this.increaseValue(this.slotSuccessfulReservationsPerUser, key);
        }

    }
  
  public toString(): string {
      let src: string='';
    this.bikeFailedReservationsPerUser.forEach( (absoluteValue, key) => 
        src += 'User: '+key+' '+absoluteValue.name+': '+absoluteValue.value+'\n');
    this.bikeSuccessfulReservationsPerUser.forEach( (absoluteValue, key) => 
        src += 'User: '+key+' '+absoluteValue.name+': '+absoluteValue.value+'\n'); 
    this.slotFailedReservationsPerUser.forEach( (absoluteValue, key) => 
        src += 'User: '+key+' '+absoluteValue.name+': '+absoluteValue.value+'\n');
    this.slotSuccessfulReservationsPerUser.forEach( (absoluteValue, key) => 
        src += 'User: '+key+' '+absoluteValue.name+': '+absoluteValue.value+'\n');
    
    return src;
  }
}