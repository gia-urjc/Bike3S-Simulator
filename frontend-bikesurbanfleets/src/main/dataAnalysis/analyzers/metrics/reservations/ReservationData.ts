import { Entity } from '../../../systemDataTypes/Entities';
import { Data } from '../../Data';
import { AbsoluteValue } from '../../AbsoluteValue';
import { ReservationAbsoluteValue } from "./ReservationAbsoluteValue";

export class ReservationData implements Data {
    static readonly NAMES: Array<string> = [
        'Successful bike reservations',
        'Failed bike reservations',
        'Successful slot reservations',
        'Failed slot reservations'];
    absoluteValues: Map<number, AbsoluteValue>;
    
    public constructor() {
        this. absoluteValues = new Map();
    }
    
    public increaseSuccessfulBikeReservations(key: number): void {
        let absValue: AbsoluteValue | undefined = this.absoluteValues.get(key);        
        if (absValue !== undefined) {
            absValue.successfulBikeReservations++;                
        }
    }
    
    public increaseFailedBikeReservations(key: number): void {
        let absValue: AbsoluteValue | undefined = this.absoluteValues.get(key);        
        if (absValue !== undefined) {
            absValue.failedBikeReservations++;                
        }
    }
    
    public increaseSuccessfulSlotReservations(key: number): void {
        let absValue: AbsoluteValue | undefined = this.absoluteValues.get(key);        
        if (absValue !== undefined) {
            absValue.successfulSlotReservations++;                
        }
    }
        
    public increaseFailedSlotReservations(key: number): void {
        let absValue: AbsoluteValue | undefined = this.absoluteValues.get(key);        
        if (absValue !== undefined) {
            absValue.failedSlotReservations++;                
        }
    }
   
    public init(entities: Array<Entity>): void {
        for(let entity of entities) {
            this.absoluteValues.set(entity.id, new ReservationAbsoluteValue());                
        }
    }
       
}