import { HistoryEntitiesJson } from "../../../../shared/history";
import { HistoryReader } from '../../../util/';
import { HistoryIterator } from '../../HistoryIterator';
import { Reservation } from '../../systemDataTypes/Entities';
import { Observer, Observable } from '../ObserverPattern';
import { Calculator } from "./Calculator";

export class ReservationCalculator implements Calculator {
    private reservations: Array<Reservation>;
    private observers: Array<Observer>;
    
    public constructor() {
        this.observers = new Array<Observer>();
    }
    
    public setReservations(reservations: Array<Reservation>): void {
        this.reservations = reservations;
    }
    
    public async calculate(): Promise<void> {
        for (let reservation of this.reservations) {
            this.notify(reservation);
        }
        return;
    }
    
    public notify(reservation: Reservation): void {
        for(let observer of this.observers) {
            observer.update(reservation);
        }
    }
    
    public subscribe(observer: Observer): void {
        this.observers.push(observer);
    }

}