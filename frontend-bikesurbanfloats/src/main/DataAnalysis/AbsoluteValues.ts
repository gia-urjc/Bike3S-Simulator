import { HistoryReader } from '../util';

import { HistoryEntities } from '../../shared/history';
import { HistoryIterator } from './HistoryIterator';

    
export class AbsoluteValues {
    private entities: HistoryEntities; 
    // key: user id
    private bikeFailedReservations: Map<number, number>;
    private slotFailedReservations: Map<number, number>;
    private bikeSuccessfulReservations: Map<number, number>;
    private slotSuccessfulReservations: Map<number, number>;
    private bikeRentalAttempts: Map<number, number>;
    private bikeReturnAttempts: Map<number, number>;
    
    public async init(path: string): Promise<void> {
        let history: HistoryReader = await HistoryReader.create(path);
        this.entities = await history.readEntities();
        let users: Array<any> = this.entities.users;
        
        for(let user of users) {
            this.bikeFailedReservations.set(user.id, 0);
            this.slotFailedReservations.set(user.id, 0);            
            this.bikeSuccessfulReservations.set(user.id, 0);
            this.slotSuccessfulReservations.set(user.id, 0);
            this.bikeRentalAttempts.set(user.id, 0);
            this.bikeReturnAttempts.set(user.id, 0);            
        }
        return;
    }
    
    public async calculateAbsolutesValues(path: string): Promise<void> {
        let it: HistoryIterator = await HistoryIterator.create(path);
        let timeEntry: any = it.nextTimeEntry();
        
        this.updateReservations();
        
        while(timeEntry !== undefined) {
            //updateBikeRentalAttempts(timeEntry);
            //updateBikeReturnAttempts(timeEntry);
            timeEntry = it.nextTimeEntry();
        }
        return;
    }
        
    public updateReservations(): void {
        let reservations: Array<any> = this.entities.reservations;
        let value: number | undefined;
        let key: number;
        
        for (let reservation of reservations) {
            key = reservation.user.id;
            if (reservation.type === "BIKE" && reservation.state === "FAILED") {
               value = this.bikeFailedReservations.get(key);
                if (value !== undefined) 
               this.bikeFailedReservations.set(key, ++value);
            }
            else if (reservation.type === "BIKE" && reservation.state !== "FAILED") {
                value = this.bikeSuccessfulReservations.get(key);
                if (value !== undefined)
                    this.bikeSuccessfulReservations.set(key, ++value);
            }     
            else if (reservation.type === "SLOT" && reservation.state === "FAILED") {
               value = this.slotFailedReservations.get(key);
               if (value !== undefined)                 
                this.slotFailedReservations.set(key, ++value);
            }
            else if (reservation.type === "SLOT" && reservation.state !== "FAILED") {
               value = this.slotSuccessfulReservations.get(key);
                if (value !== undefined)                                
                this.slotSuccessfulReservations.set(key, ++value);
            }
        }        
    }
 
        
}