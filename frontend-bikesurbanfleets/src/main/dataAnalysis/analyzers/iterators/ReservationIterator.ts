import { Reservation } from '../../systemDataTypes/Entities';
import { Observer } from '../ObserverPattern';
import { Iterator } from "./Iterator";

export class ReservationIterator implements Iterator {
    private reservations: Array<Reservation>;
    private observers: Array<Observer>;
    
    public constructor() {
        this.observers = new Array<Observer>();
    }
    
    public setReservations(reservations: Array<Reservation>): void {
        this.reservations = reservations;
    }
    
    public iterate(): void {
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