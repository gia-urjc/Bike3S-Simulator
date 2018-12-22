import { Data } from '../../Data';
import { AbsoluteValue } from '../../AbsoluteValue';
import { ReservationAbsoluteValue } from "./ReservationAbsoluteValue";

export class ReservationData implements Data {
    static readonly NAMES: Array<string> = ['Successful bike reservations', 'Failed bike reservations', 'Successful slot reservations', 'Failed slot reservations'];
    absoluteValues: Map<number, ReservationAbsoluteValue>;
    
    public constructor() {
        this. absoluteValues = new Map();
    }
    
    private getElement(key: number): ReservationAbsoluteValue {
        let absValue: ReservationAbsoluteValue | undefined = this.absoluteValues.get(key);
        if (!absValue) {
            absValue=new ReservationAbsoluteValue();  // a gotten map value could be undefined
            this.absoluteValues.set(key, absValue);
        } 
        return absValue;  
    }
    
    
    public increaseSuccessfulBikeReservations(key: number): void {
        this.getElement(key).successfulBikeReservations++;                
    }
    
    public increaseFailedBikeReservations(key: number): void {
        this.getElement(key).failedBikeReservations++;                
    }
    
    public increaseSuccessfulSlotReservations(key: number): void {
        this.getElement(key).successfulSlotReservations++;                
    }
        
    public increaseFailedSlotReservations(key: number): void {
        this.getElement(key).failedSlotReservations++;                
    }
}
     
 