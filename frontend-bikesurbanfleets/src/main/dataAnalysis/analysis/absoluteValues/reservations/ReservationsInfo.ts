import { Entity } from '../../../systemDataTypes/Entities';
import { Data } from "../Data";
import { Info } from '../Info';

export class ReservationsInfo implements Info {
    private successfulBikeReservations: Data;
    private failedBikeReservations: Data;
    private successfulSlotReservations: Data;
    private failedSlotReservations: Data;
    
    public constructor() {
        this.successfulBikeReservations = { name: 'Successful bike reservations', value: new Map<number, number>() };
        this.failedBikeReservations = { name: 'Failed bike reservations', value: new Map<number, number>() };
        this.successfulSlotReservations = { name: 'Successful slot reservations', value: new Map<number, number>() };
        this.failedSlotReservations = { name: 'Failed slot reservations', value: new Map<number, number>() };
    }
    
    public getSuccessfulBikeReservations(): Data {
        return this.successfulBikeReservations;
    }
    
    public getFailedBikeReservations(): Data {
        return this.failedBikeReservations;
    }
    
    public getSuccessfulSlotReservations(): Data {
        return this.successfulSlotReservations;
    }
    
    public getFiledSlotReservations(): Data {
        return this.failedSlotReservations;
    }
    
    public increaseSuccessfulBikeReservations(key: number): void {
        let value: number | undefined = this.successfulBikeReservations.value.get(key);        
        if (value !== undefined) {
            this.successfulBikeReservations.value.set(key, ++value);                
        }
    }
    
    public increaseFailedBikeReservations(key: number): void {
        let value: number | undefined = this.failedBikeReservations.value.get(key);        
        if (value !== undefined) {
            this.failedBikeReservations.value.set(key, ++value);                
        }
    }
    
    public increaseSuccessfulSlotReservations(key: number): void {
        let value: number | undefined = this.successfulSlotReservations.value.get(key);        
        if (value !== undefined) {
            this.successfulSlotReservations.value.set(key, ++value);                
        }        
    }
    
    public increaseFailedSlotReservations(key: number): void {
        let value: number | undefined = this.failedSlotReservations.value.get(key);        
        if (value !== undefined) {
            this.failedSlotReservations.value.set(key, ++value);                
        }        
    }
    
    public async initData(entities: Array<Entity>): Promise<void> {
        for(let entity of entities) {
            this.successfulBikeReservations.value.set(entity.id, 0);
            this.failedBikeReservations.value.set(entity.id, 0);
            this.successfulSlotReservations.value.set(entity.id, 0);
            this.failedSlotReservations.value.set(entity.id, 0);                
        }        
        return;
    }
    
       
}