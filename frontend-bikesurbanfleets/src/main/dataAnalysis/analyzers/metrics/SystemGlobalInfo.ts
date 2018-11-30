
import { SystemInfo } from "../SystemInfo";

export class SystemGlobalInfo {
    static readonly NAMES: Array<string> = [
        'Demand satisfaction',
        'Hire eficiency',
        'Return eficiency'
    ];
  private demandSatisfaction: number;
  private hireEfficiency: number;
  private returnEfficiency: number;
  
  public constructor() {
   
    this.demandSatisfaction = 0;
    this.hireEfficiency = 0;
    this.returnEfficiency = 0;
  }
    
  public calculateGlobalData(reservations: SystemInfo, rentalsAndReturns: SystemInfo, users:number): void {

  let numberUsers: number=users;

  /*let totalRentals: number = 0;
  let totalReturns: number = 0; 
  let totalBikeReservations: number = 0;
  let totalSlotReservations: number = 0;*/
  
  let successfulRentals: number = 0;
  let successfulReturns: number = 0;
  let successfulBikeReservations: number = 0;
  let successfulSlotReservations: number = 0;  
  
  let failedRentals: number = 0;
  let failedReturns: number = 0;
  let failedBikeReservations: number = 0;
  let failedSlotReservations: number = 0;
    
    reservations.getData().absoluteValues.forEach( (v, k) => successfulBikeReservations += v.successfulBikeReservations); 
    reservations.getData().absoluteValues.forEach( (v, k) => successfulSlotReservations += v.successfulSlotReservations);
    reservations.getData().absoluteValues.forEach( (v, k) => failedBikeReservations += v.failedBikeReservations);
    reservations.getData().absoluteValues.forEach( (v, k) => failedSlotReservations += v.failedSlotReservations);
    
    //totalBikeReservations = successfulBikeReservations + failedBikeReservations;
    //totalSlotReservations = successfulSlotReservations + failedSlotReservations;
    
    rentalsAndReturns.getData().absoluteValues.forEach( (v, k) => successfulRentals += v.successfulRentals);
    rentalsAndReturns.getData().absoluteValues.forEach( (v, k) => successfulReturns += v.successfulReturns);
    rentalsAndReturns.getData().absoluteValues.forEach( (v, k) => {
        if(v.successfulRentals === 1) {
            failedRentals += v.failedRentals;
        }
    });
    rentalsAndReturns.getData().absoluteValues.forEach( (v, k) => failedReturns += v.failedReturns);
    
    //totalRentals = successfulRentals + failedRentals;
    //totalReturns = successfulReturns + failedReturns;
    
    this.demandSatisfaction = successfulRentals / numberUsers;  // DS = SH / N
    this.hireEfficiency = successfulRentals / (successfulRentals + failedRentals);  // HE = SH / (N+FH)
    this.returnEfficiency = successfulReturns / (successfulReturns + failedReturns); // RE = SR / (SH+FR)
  }

  public getDemandSatisfaction(): number {
    return this.demandSatisfaction;
  }

  public getHireEfficiency(): number {
    return this.hireEfficiency;
  }

  public getReturnEfficiency(): number {
    return this.returnEfficiency;
  }

  public getDataAsArray(): Array<number> {
      let array: Array<number> = new Array();
      array.push(this.demandSatisfaction);
      array.push(this.hireEfficiency);
      array.push(this.returnEfficiency);
      return array;
  }   
   
}