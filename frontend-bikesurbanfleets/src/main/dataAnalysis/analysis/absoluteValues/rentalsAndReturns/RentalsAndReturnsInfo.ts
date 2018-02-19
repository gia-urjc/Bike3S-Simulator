import { Entity } from '../../../systemDataTypes/Entities';
import { Info } from '../Info';
import { Data } from "../Data";

export class RentalsAndReturnsInfo implements Info {
  private factType: string;
  private entityType: string;
  
  private successfulRentals: Info; 
  private failedRentals: Info;
  private successfulReturns: Info;
  private failedReturns: Info;
  
  public constructor(entity: string) {
    this.factType = 'RENTAL_AND_RETURN';
    this.entityType = entity;
    
    this.successfulRentals = { name: 'Successful bike rentals', value: new Map<number, number>() };
    this.failedRentals = { name: 'Failed bike rentals', value: new Map<number, number>() };
    this.successfulReturns = { name: 'Successful bike returns', value: new Map<number, number>() };
        this.failedReturns = { name: 'Failed bike returns', value: new Map<number, number>() };
    
  }
  
  public getFactType(): string {
    return this.factType;
  }
  
  public getEntityType(): string {
    return this.entityType;
  }
  
  public getSuccessfulRentals(): Map<number, number> { 
    return this.successfulRentals.value;
  }
  
  public getFailedRentals(): Map<number, number> { 
    return this.failedRentals.value;
  }
  
  public getSuccessfulReturns(): Map<number, number> { 
    return this.successfulReturns.value;
  }
  
  public getFailedReturns(): Map<number, number> { 
    return this.failedReturns.value;
  }
  
  public increaseSuccessfulRentals(key: number | undefined): void {
    if (key !== undefined) {
      let value: number | undefined = this.successfulRentals.get(key);
      if (value !== undefined) {  // a gotten map value could be undefined
          this.successfulRentals.set(key, ++value);
      }
    }
  }
    
  public increaseFailedRentals(key: number | undefined): void {
    if (key !== undefined) {
      let value: number | undefined = this.failedRentals.get(key);
      if (value !== undefined) {  // a gotten map value could be undefined
          this.failedRentals.set(key, ++value);
      }
    }
  }

  public increaseSuccessfulReturns(key: number | undefined): void {
    if (key !== undefined) {
      let value: number | undefined = this.successfulReturns.get(key);
      if (value !== undefined) {  // a gotten map value could be undefined
          this.successfulReturns.set(key, ++value);
      }
    }
  }
    
  public increaseFailedReturns(key: number | undefined): void {
    if (key !== undefined) {
      let value: number | undefined = this.failedReturns.get(key);
      if (value !== undefined) {  // a gotten map value could be undefined
          this.failedReturns.set(key, ++value);
      }
    }
  }
    
    public async initData(entities: Array<Entity>): Promise<void> {
        for(let entity of entities) {
             this.getFailedRentals().set(entityy.id, 0);
             this.getSuccessfulRentals().set(entityy.id, 0);
             this.getFailedReturns().set(entityy.id, 0);            
             this.getSuccessfulReturns().set(entityy.id, 0);            
        }
        }
    return;
    }

}