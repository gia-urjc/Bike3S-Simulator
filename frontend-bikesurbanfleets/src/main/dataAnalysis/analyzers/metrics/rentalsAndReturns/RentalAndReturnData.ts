import { Data } from '../../Data';
import { AbsoluteValue } from '../../AbsoluteValue';
import { RentalAndReturnAbsoluteValue } from "./RentalAndReturnAbsoluteValue";

export class RentalAndReturnData implements Data {
    static readonly NAMES: Array<string> = ['Successful bike rentals', 'Failed bike rentals', 'Successful bike returns','Failed bike returns'];
    absoluteValues: Map<number, RentalAndReturnAbsoluteValue>;
    
    public constructor() {
        this.absoluteValues = new Map();
    }
  
    private getElement(key: number): RentalAndReturnAbsoluteValue {
        let absValue: RentalAndReturnAbsoluteValue | undefined = this.absoluteValues.get(key);
        if (!absValue) {
            absValue=new RentalAndReturnAbsoluteValue();  // a gotten map value could be undefined
            this.absoluteValues.set(key, absValue);
        } 
        return absValue;  
    }
    
    public increaseSuccessfulRentals(key: number): void {
        this.getElement(key).successfulRentals++;
    }
    
    public increaseFailedRentals(key: number): void {
        this.getElement(key).failedRentals++;
    }

    public increaseSuccessfulReturns(key: number): void {
        this.getElement(key).successfulReturns++;
    }
    
    public increaseFailedReturns(key: number): void {
        this.getElement(key).failedReturns++;
    }

}