import { HistoryReader } from '../../../util/HistoryReader';
import { HistoryIterator } from '../../HistoryIterator';
import { Reservation } from '../../systemDataTypes/Entities';
import { Observer, Observable } from '../ObserverPattern';

export class ReservationsIterator implements Observable {
    private reservations: Array<Reservation>;
    private observers: Array<Observer>;
    
    private constructor() {
        this.observers = new Array<Observer>();
    }
    
    private async init(path: string): Promise<void> {
        let history: HistoryReader = await HistoryReader.create(path);
        this.reservations = await history.getEntities("reservations").instances;
    }
    
    public static async create(path: string): Promise<ReservationsIterator> {
        let it = new ReservationsIterator();
        await it.init(path);
        return it;
    }
    
    public async calculateReservations(): Promise<void> {
        for (let reservation of this.reservations) {
            this.notify(reservation);
        }
    }
    
    public notify(reservation: Reservation): void {
        for(let observer of this.observers) {
            observer.update(reservation);
        }
    }
    
    public subbscribe(observer: Observer): void {
        this.observers.push(observer);
    }

}