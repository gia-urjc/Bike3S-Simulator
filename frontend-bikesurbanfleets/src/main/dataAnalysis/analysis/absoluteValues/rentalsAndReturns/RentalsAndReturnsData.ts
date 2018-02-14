import { Info } from "../Info";

export class RentalsAndReturnsData implements Data {
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
    return this.successfulRentals;
  }
  
  public getFailedRentals(): Map<number, number> { 
    return this.failedRentals;
  }
  
  public getSuccessfulReturns(): Map<number, number> { 
    return this.successfulReturns;
  }
  
  public getFailedReturns(): Map<number, number> { 
    return this.failedReturns;
  }
  
   public increaseSuccessfulRentals(key: number | undefined): void {
    if (key !== undefined) {
      let value: number | undefined = this.successfulRentals.get(key);
      if (value !== undefined) {  // a gotten map value could be undefined
          value++;
      }
    }
 }
    
 
  
}