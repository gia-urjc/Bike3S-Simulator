import { AbsoluteValue } from "../../AbsoluteValue";

export class RentalAndReturnAbsoluteValue implements AbsoluteValue {
    static readonly NUM_ATTR: number = 4;
    successfulRentals: number; 
    failedRentals: number;
    successfulReturns: number;
    failedReturns: number;
    
    public constructor() {
        this.successfulRentals = 0;
        this.failedRentals = 0;
        this.successfulReturns = 0;
        this.failedReturns = 0;
    }
    
  getAbsoluteValuesAsArray(): Array<number> {
      let array: Array<number> = new Array();
      array.push(this.successfulRentals);
      array.push(this.failedRentals);
      array.push(this.successfulReturns);
      array.push(this.failedReturns);
      return array;
  }

}