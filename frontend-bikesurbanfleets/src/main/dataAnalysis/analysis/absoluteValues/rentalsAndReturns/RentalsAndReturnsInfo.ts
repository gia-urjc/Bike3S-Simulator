import { Entity } from '../../../systemDataTypes/Entities';
import { Info } from '../Info';
import { Data } from "../Data";

export class RentalsAndReturnsInfo {
    private static readonly SUCCESSFUL_RENTALS: string = 'Successful_bike_rentals'; 
    private static readonly FAILED_RENTALS: string = 'Failed_bike_rentals';
    private static readonly SUCCESSFUL_RETURNS: string = 'Successful_bike_returns';
    private static readonly FAILED_RETURNS: string = 'Failed_bike_returns';
    
  private successfulRentals: Data; 
  private failedRentals: Data;
  private successfulReturns: Data;
  private failedReturns: Data;
  
  public constructor() {
    this.successfulRentals = { name: RentalsAndReturnsInfo.SUCCESSFUL_RENTALS, value: new Map<number, number>() };
    this.failedRentals = { name: RentalsAndReturnsInfo.FAILED_RENTALS, value: new Map<number, number>() };
    this.successfulReturns = { name: RentalsAndReturnsInfo.SUCCESSFUL_RETURNS, value: new Map<number, number>() };
        this.failedReturns = { name: RentalsAndReturnsInfo.FAILED_RETURNS, value: new Map<number, number>() };
  }
  
  public getSuccessfulRentals(): Data { 
    return this.successfulRentals;
  }
  
  public getFailedRentals(): Data { 
    return this.failedRentals;
  }
  
  public getSuccessfulReturns(): Data { 
    return this.successfulReturns;
  }
  
  public getFailedReturns(): Data { 
    return this.failedReturns;
  }
  
  public increaseSuccessfulRentals(key: number): void {
      let value: number | undefined = this.successfulRentals.value.get(key);
      if (value !== undefined) {  // a gotten map value could be undefined
          this.successfulRentals.value.set(key, ++value);
      }
  }
    
  public increaseFailedRentals(key: number): void {
      let value: number | undefined = this.failedRentals.value.get(key);
      if (value !== undefined) {  // a gotten map value could be undefined
          this.failedRentals.value.set(key, ++value);
      }
  }

  public increaseSuccessfulReturns(key: number): void {
      let value: number | undefined = this.successfulReturns.value.get(key);
      if (value !== undefined) {  // a gotten map value could be undefined
          this.successfulReturns.value.set(key, ++value);
      }
  }
    
  public increaseFailedReturns(key: number): void {
      let value: number | undefined = this.failedReturns.value.get(key);
      if (value !== undefined) {  // a gotten map value could be undefined
          this.failedReturns.value.set(key, ++value);
      }
  }
    
    public async initData(entities: Array<Entity>): Promise<void> {
        for(let entity of entities) {
             this.failedRentals.value.set(entity.id, 0);
             this.successfulRentals.value.set(entity.id, 0);
             this.failedReturns.value.set(entity.id, 0);            
             this.successfulReturns.value.set(entity.id, 0);            
        }
        return;
    }
    
    public static getNames(): Array<string> {
        let names: Array<string> = new Array();
        names.push(RentalsAndReturnsInfo.SUCCESSFUL_RENTALS);
        names.push(RentalsAndReturnsInfo.FAILED_RENTALS);
        names.push(RentalsAndReturnsInfo.SUCCESSFUL_RETURNS);
        names.push(RentalsAndReturnsInfo.FAILED_RETURNS);
        return names;
    }
    
    public getData(): Array<Data> {
        let data: Array<Data> = new Array();
        data.push(this.successfulRentals);
        data.push(this.failedRentals);
        data.push(this.successfulReturns);
        data.push(this.failedReturns);
        return data;
    }
  
}