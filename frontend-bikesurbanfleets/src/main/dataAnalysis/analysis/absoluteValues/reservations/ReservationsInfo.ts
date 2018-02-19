import { Entity } from '../../../systemDataTypes/Entities';
import { Info } from '../Info';

export class ReservationsInfo implements Info {
    private factType: string;
    private entityType: string;
    
    private successfulBikeReservations: Info;
    private failedBikeReservations: Info;
    private successfulSlotReservations: Info;
    private failedSlotReservations: Info;
    
    public constructor(entity: string) {
        this.factType = 'RESERVATION';
        this.entityType = entity;
        
        this.successfulBikeReservations = { name: 'Successful bike reservations', value: new Map<number, number>() };
        this.failedBikeReservations = { name: 'Failed bike reservations', value: new Map<number, number>() };
        this.successfulSlotReservations = { name: 'Successful slot reservations', value: new Map<number, number>() };
        this.failedSlotReservations = { name: 'Failed slot reservations', value: new Map<number, number>() };
    }
    
    public getFactType(): string {
        return this.factType;
    }
    
    public getEntityType(): string {
        return this.entityType;
    }
    
    public getSuccessfulBikeReservations(): Map<number, number> {
        return this.successfulBikeReservations;
    }
    
    public getFailedBikeReservations(): Map<number, number> {
        return this.failedBikeReservations;
    }
    
    public getSuccessfulSlotReservations(): Map<number, number> {
        return this.successfulSlotReservations;
    }
    
    public getFiledSlotReservations(): Map<number, number> {
        return this.failedSlotReservations;
    }
    
    public increaseSuccessfulBikeReservations(key: number): void {
        let value: number = this.successfulBikeReservations.get(key);        
        if (value !== undefined) {
            this.successfulBikeReservations.set(key, ++value);                
        }
    }
    
    public increaseFailedBikeReservations(key: number): void {
        let value: number = this.failedBikeReservations(key);        
        if (value !== undefined) {
            this.failedBikeReservations.set(key, ++value);                
        }
    }
    
    public increaseSuccessfulSlotReservations(key: number): void {
        let value: number = this.successfulSlotReservations.get(key);        
        if (value !== undefined) {
            this.successfulSlotReservations.set(key, ++value);                
        }        
    }
    
    public increaseFailedSlotReservations(): Map<number, number> {
        let value: number = this.failedSlotReservations.get(key);        
        if (value !== undefined) {
            this.failedSlotReservations.set(key, ++value);                
        }        
    }
    
    public async initData(entities: Array<Entity>): Promise<void> {
        for(let entity of entities) {
            this.getSuccessfulBikeReservations().set(entity.id, 0);
            this.getFailedBikeReservations().set(entity.id, 0);
            this.getSuccessfulSlotReservations().set(entity.id, 0);
            this.getFailedSlotReservations().set(entity.id, 0);                
        }        
        return;
    }
        
}