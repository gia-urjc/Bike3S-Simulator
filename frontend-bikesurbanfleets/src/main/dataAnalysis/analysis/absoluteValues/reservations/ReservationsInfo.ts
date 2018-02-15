import { Entity } from '../../../systemDataTypes/Entities';
import { Info } from '../Info';

export abstract class ReservationsInfo implements Info {
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
        
    }
    
    public increaseFailedBikeReservations(key: number): void {
    }
    
    public increaseSuccessfulSlotReservations(key: number): void {
        
    }
    
    public increaseFailedSlotReservations(): Map<number, number> {
        return this.successfulBikeReservations;
    }
    
    public async initData(entities: Array<Entity>): Promise<void> {
        for(let entity of entities) {
            this.getSuccessfulBikeReservations().set(entity.id, 0);
            this.getFailedBikeReservations().set(entity.id, 0);
            this.getSuccessfulSlotReservations().set(entity.id, 0);
            this.getFailedSlotReservations().set(entity.id, 0);                
        }        
    }
    
    abstract update(): void;
    
        
}