import { HistoryReader } from '../../../../util';
import { HistoryEntities } from '../../../../../shared/history';
import { Observer } from '../../ObserverPattern';
import  { User, Reservation } from '../../../dataTypes/Entities';
import  { TimeEntry } from '../../../dataTypes/SystemInternalData';
import { BikeFailedReservations, BikeSuccessfulReservations,
    SlotFailedReservations, SlotSuccessfulReservations } from '../userDataDefinitions/RentalsAndReturnsPerUser';    

export class ReservationsAbsoluteValues implements Observer {
    private users: Array<User>;
    private bikeFailedReservationsPerUser: BikeFailedReservations; 
    private slotFailedReservationsPerUser: SlotFailedReservations;
    private bikeSuccessfulReservationsPerUser: BikeSuccessfulReservations;
    private slotSuccessfulReservationsPerUser: SlotSuccessfulReservations;
    
    private constructor() {
        this.bikeFailedReservationsPerUser = new BikeFailedReservations(); 
        this.slotFailedReservationsPerUser = new SlotFailedReservations();
        this.slotFailedReservationsPerUser = new SlotFailedReservations();
        this.slotSuccessfulReservationsPerUser = new SlotSuccessfulReservations();
    }
    
    private async init(path: string): Promise<void> {
        let history: HistoryReader = await HistoryReader.create(path);
        let entities = await history.readEntities();
        this.users = entities.users;
        
        for(let user of this.users) {
            this.bikeFailedReservationsPerUser.getMap().set(user.id, 0);
            this.slotFailedReservationsPerUser.getMap().set(user.id, 0);            
            this.bikeSuccessfulReservationsPerUser.getMap().set(user.id, 0);
            this.slotSuccessfulReservationsPerUser.getMap().set(user.id, 0);
        }
    }
   
    public static async create(path: string): Promise<ReservationsAbsoluteValues> {
        let reservationValues = new ReservationsAbsoluteValues();
        await reservationValues.init(path);

        return reservationValues;
    }
    
    public getBikeFailedReservationsOfUser(userId: number): number| undefined {
     return this.bikeFailedReservationsPerUser.getMap().get(userId);
    }

    public getSlotFailedReservationsOfUser(userId: number): number | undefined {
        return this.slotFailedReservationsPerUser.getMap().get(userId);
    }
    
    public getBikeSuccessfulReservationsOfUser(userId: number): number | undefined {
        return this.bikeSuccessfulReservationsPerUser.getMap().get(userId);
    }
    
    public getSlotSuccessfulReservationsOfUser(userId: number): number | undefined {
        return this.slotSuccessfulReservationsPerUser.getMap().get(userId);
    }
        
    public update(reservation: Reservation): void {
        this.bikeFailedReservationsPerUser.update(reservation); 
        this.slotFailedReservationsPerUser.update(reservation);
        this.slotFailedReservationsPerUser.update(reservation);
        this.slotSuccessfulReservationsPerUser.update(reservation);
    }

               
}