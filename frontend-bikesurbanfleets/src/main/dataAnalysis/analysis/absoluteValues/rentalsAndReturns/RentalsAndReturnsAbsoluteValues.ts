import { AbsoluteValue } from "../AbsoluteValue";

export class RentalsAndReturnsAbsoluteValues implements AbsoluteValue {
  successfulRentals: number; 
  failedRentals: number;
  successfulReturns: number;
  failedReturns: number;
    
  getAbsoluteValuesAsArray(): Array<number> {
      let array: Array<number> = new Array();
      array.push(this.successfulRentals);
      array.push(this.failedRentals);
      array.push(this.successfulReturns);
      array.push(this.failedReturns);
      return array;
  }

}