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
    private bikeFailedRentals: Map<number, number>;
    private bikeSuccessfulRentals: Map<number, number>;
    private bikeFailedReturns: Map<number, number>;
    private bikeSuccessfulReturns: Map<number, number>;
    
    public constructor() {
        this.bikeFailedReservations = new Map<number, number>();
        this.slotFailedReservations = new Map<number, number>();
        this.bikeSuccessfulReservations = new Map<number, number>();
        this.slotSuccessfulReservations = new Map<number, number>();
        this.bikeFailedRentals = new Map<number, number>();
        this.bikeSuccessfulRentals = new Map<number, number>();
        this.bikeFailedReturns = new Map<number, number>();
        this.bikeSuccessfulReturns = new Map<number, number>();
    }
    
    public getBikeFailedReservations(userId: number): number| undefined {
        return this.bikeFailedReservations.get(userId);
    }
    
    public getSlotFailedReservations(userId: number): number | undefined {
        return this.slotFailedReservations.get(userId);
    }
    
    public getBikeSuccessfulReservations(userId: number): number | undefined {
        return this.bikeSuccessfulReservations.get(userId);
    }

    public getSlotSuccessfulReservations(userId: number): number | undefined {
        return this.slotSuccessfulReservations.get(userId);
    }
    
    public getBikeFailedRentals(userId: number): number | undefined {
        return this.bikeFailedRentals.get(userId);
    }
    
    public getBikeSuccessfulRentals(userId: number): number | undefined {
        return this.bikeSuccessfulRentals.get(userId);
    }
    
    public getBikeFailedReturns(userId: number): number | undefined {
        return this.bikeFailedReturns.get(userId);
    }
    
    public getBikeSuccessfulReturns(userId: number): number | undefined {
        return this.bikeSuccessfulReturns.get(userId);
    }
    
    public async init(path: string): Promise<void> {
        let history: HistoryReader = await HistoryReader.create(path);
        this.entities = await history.readEntities();
        let users: Array<any> = this.entities.users;
        
        for(let user of users) {
            this.bikeFailedReservations.set(user.id, 0);
            this.slotFailedReservations.set(user.id, 0);            
            this.bikeSuccessfulReservations.set(user.id, 0);
            this.slotSuccessfulReservations.set(user.id, 0);
            this.bikeFailedRentals.set(user.id, 0);
            this.bikeSuccessfulRentals.set(user.id, 0);
            this.bikeFailedReturns.set(user.id, 0);            
            this.bikeSuccessfulReturns.set(user.id, 0);            
        }
        return;
    }
    
    public async calculateAbsolutesValues(path: string): Promise<void> {
        let it: HistoryIterator = await HistoryIterator.create(path);
        let timeEntry: any = it.nextTimeEntry();
        
        await this.init(path);
        this.calculateReservations();
        
        while(timeEntry !== undefined) {
            //updateBikeRentalAttempts(timeEntry);
            //updateBikeReturnAttempts(timeEntry);
            timeEntry = it.nextTimeEntry();
        }
        return;
    }
        
    public calculateReservations(): void {
        let reservations: Array<any> = this.entities.reservations;
        let value: number | undefined;
        let key: number;
        
        for (let reservation of reservations) {
            key = reservation.user.id;
            console.log(key);
            if (reservation.type === "BIKE" && reservation.state === "FAILED") {
               value = this.bikeFailedReservations.get(key);
                console.log("bike failed");
               if (value !== undefined) { 
                this.bikeFailedReservations.set(key, ++value);
               }
            }
            else if (reservation.type === "BIKE" && reservation.state !== "FAILED") {
                value = this.bikeSuccessfulReservations.get(key);
                console.log("bike successful");
                if (value !== undefined) {
                    this.bikeSuccessfulReservations.set(key, ++value);
                }
            }     
            else if (reservation.type === "SLOT" && reservation.state === "FAILED") {
               value = this.slotFailedReservations.get(key);
                console.log("slot failed");
               if (value !== undefined) {                 
                this.slotFailedReservations.set(key, ++value);
               }
            }
            else if (reservation.type === "SLOT" && reservation.state !== "FAILED") {
               value = this.slotSuccessfulReservations.get(key);
                console.log("slotsuccessful");
               if (value !== undefined) {                                
                this.slotSuccessfulReservations.set(key, ++value);
               }
            }
        }        
    }
 
        
}