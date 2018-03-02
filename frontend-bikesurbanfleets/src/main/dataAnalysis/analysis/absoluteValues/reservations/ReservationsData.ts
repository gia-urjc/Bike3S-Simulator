import { Entity } from '../../../systemDataTypes/Entities';
import { Data } from '../Data';
import { AbsoluteValue } from '../AbsoluteValue';
import { ReservationsAbsoluteValues } from "./ReservationsAbsoluteValues";

export class ReservationsData implements Data {
    static readonly NAMES: Array<string> = ['Successful bike reservations', 'Failed bike reservations', 'Successful slot reservations', 'Failed slot reservations'];
    absoluteValues: Map<number, AbsoluteValue>;
    
    public constructor() {
        this.absoluteValues = new Map();
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
   
    public async init(entities: Array<Entity>): Promise<void> {
        for(let entity of entities) {
            this.absoluteValues.set(entity.id, new ReservationsAbsoluteValues());                
        }        
        return;
    }
       
}