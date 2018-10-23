import { Entity } from '../../../systemDataTypes/Entities';
import { Data } from '../../Data';
import { AbsoluteValue } from '../../AbsoluteValue';
import { RentalAndReturnAbsoluteValue } from "./RentalAndReturnAbsoluteValue";

export class RentalAndReturnData implements Data {
    static readonly NAMES: Array<string> = ['Successful bike rentals', 'Failed bike rentals', 'Successful bike returns','Failed bike returns'];
    absoluteValues: Map<number, AbsoluteValue>;
    
    public constructor() {
        this.absoluteValues = new Map();
    }
  
    public increaseSuccessfulRentals(key: number): void {
        let absValue: AbsoluteValue | undefined = this.absoluteValues.get(key);
        if (absValue !== undefined) {  // a gotten map value could be undefined
            absValue.successfulRentals++;
        }
    }
    
    public increaseFailedRentals(key: number): void {
        let absValue: AbsoluteValue | undefined = this.absoluteValues.get(key);
        if (absValue !== undefined) {  // a gotten map value could be undefined
            absValue.failedRentals++;
        }
    }

    public increaseSuccessfulReturns(key: number): void {
        let absValue: AbsoluteValue | undefined = this.absoluteValues.get(key);
        if (absValue !== undefined) {  // a gotten map value could be undefined
            absValue.successfulReturns++;
        }
    }
    
    public increaseFailedReturns(key: number): void {
        let absValue: AbsoluteValue | undefined = this.absoluteValues.get(key);
        if (absValue !== undefined) {  // a gotten map value could be undefined
            absValue.failedReturns++;
        }
    }
    
    public async initData(entities: Array<Entity>): Promise<void> {
        for(let entity of entities) {
            this.absoluteValues.set(entity.id, new RentalAndReturnAbsoluteValue);
        }
        return;
    }
 
}