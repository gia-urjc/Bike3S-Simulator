import { HistoryEntitiesJson } from "../../../../shared/history";
import { HistoryReader } from '../../../util/';
import { HistoryIterator } from '../../HistoryIterator';
import { Reservation } from '../../systemDataTypes/Entities';
import { Observer, Observable } from '../ObserverPattern';
import { Calculator } from "./Calculator";
import { Sy stemReservationsInfo } from '../systemEntities/SystemReservationsInfo';

export class ReservationCalculator implements Calculator {
    private reservations: SystemReservationsInfo;
    private observers: Array<Observer>;
    
    public constructor(reservations: SystemReservationsInfo) {
        this.reservationsInfo = reservations;
        this.observers = new Array<Observer>();
    }
    
    public async calculate(): Promise<void> {
        for (let reservation of this.reservationsInfo.getReservations()) {
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