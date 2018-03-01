import { Entity } from '../../../systemDataTypes/Entities';
import { Data } from "../Data";
import { Info } from '../Info';

export class ReservationsInfo implements Info {
    private static readonly S_B_R = 'Successful_bike_reservations';
    private static readonly F_B_R = 'Failed_bike_reservations';
    private static readonly S_S_R = 'Successful_slot_reservations';
    private static readonly F_S_R = 'Failed_slot_reservations';
    
    private successfulBikeReservations: Data;
    private failedBikeReservations: Data;
    private successfulSlotReservations: Data;
    private failedSlotReservations: Data;
    
    public constructor() {
        this.successfulBikeReservations = { name: ReservationsInfo.S_B_R, value: new Map<number, number>() };
        this.failedBikeReservations = { name: ReservationsInfo.F_B_R, value: new Map<number, number>() };
        this.successfulSlotReservations = { name: ReservationsInfo.S_S_R, value: new Map<number, number>() };
        this.failedSlotReservations = { name: ReservationsInfo.F_S_R, value: new Map<number, number>() };
    }
    
    public getSuccessfulBikeReservations(): Data {
        return this.successfulBikeReservations;
    }
    
    public getFailedBikeReservations(): Data {
        return this.failedBikeReservations;
    }
    
    public getSuccessfulSlotReservations(): Data {
        return this.successfulSlotReservations;
    }
    
    public getFiledSlotReservations(): Data {
        return this.failedSlotReservations;
    }
    
    public increaseSuccessfulBikeReservations(key: number): void {
        let value: number | undefined = this.successfulBikeReservations.value.get(key);        
        if (value !== undefined) {
            this.successfulBikeReservations.value.set(key, ++value);                
        }
    }
    
    public increaseFailedBikeReservations(key: number): void {
        let value: number | undefined = this.failedBikeReservations.value.get(key);        
        if (value !== undefined) {
            this.failedBikeReservations.value.set(key, ++value);                
        }
    }
    
    public increaseSuccessfulSlotReservations(key: number): void {
        let value: number | undefined = this.successfulSlotReservations.value.get(key);        
        if (value !== undefined) {
            this.successfulSlotReservations.value.set(key, ++value);                
        }        
    }
    
    public increaseFailedSlotReservations(key: number): void {
        let value: number | undefined = this.failedSlotReservations.value.get(key);        
        if (value !== undefined) {
            this.failedSlotReservations.value.set(key, ++value);                
        }        
    }
    
    public async initData(entities: Array<Entity>): Promise<void> {
        for(let entity of entities) {
            this.successfulBikeReservations.value.set(entity.id, 0);
            this.failedBikeReservations.value.set(entity.id, 0);
            this.successfulSlotReservations.value.set(entity.id, 0);
            this.failedSlotReservations.value.set(entity.id, 0);                
        }        
        return;
    }
    
    public static getNames(): Array<string> {
        let names: Array<string> = new Array();
        names.push(ReservationsInfo.S_B_R);
        names.push(ReservationsInfo.F_B_R);
        names.push(ReservationsInfo.S_S_R);
        names.push(ReservationsInfo.F_S_R);
        return names;
    }
    
    public getData(): Array<Data> { 
        let data: Array<Data> = new Array();
        data.push(this.successfulBikeReservations);
        data.push(this.failedBikeReservations);
        data.push(this.successfulSlotReservations);
        data.push(this.failedSlotReservations);
        return data;
    }
       
}