import { HistoryEntitiesJson } from "../../../../shared/history";
import { HistoryReader } from '../../../util/';
import { HistoryIterator } from '../../HistoryIterator';
import { Reservation } from '../../systemDataTypes/Entities';
import { Observer, Observable } from '../ObserverPattern';

export class ReservationsIterator implements Observable {
    private reservations: Array<Reservation>;
    private observers: Array<Observer>;
    
    public constructor() {
        this.observers = new Array<Observer>();
      
    }
    
    public async init(path: string): Promise<void> {
        let history: HistoryReader = await HistoryReader.create(path);
        try {
            let entities: HistoryEntitiesJson = await history.getEntities("reservations");
   
        this.reservations = <Reservation[]> entities.instances;
        }
            catch(error) {
             console.log('error getting reservations: ', error);
        }
    
        return; 
    }
    
    public static async create(path: string): Promise<ReservationsIterator> {
        let it = new ReservationsIterator();
        try {
            await it.init(path);
        }
        catch(error) {
            console.log('error creating the reservations iterator: ', error);
        }
        return it;
    }
    
    public async calculateReservations(): Promise<boolean> {
        for (let reservation of this.reservations) {
            this.notify(reservation);
        }
        return true;
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