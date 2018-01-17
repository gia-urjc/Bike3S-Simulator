import { HistoryReader } from '../../../util';
import { HistoryIterator } from '../../HistoryIterator';
import { TimeEntry } from '../../systemDataTypes/SystemInternalData';
import { Observer, Observable } from '../ObserverPattern';

export class TimeEntriesIterator implements Observable {
    private observers: Array<Observer>;
    
    private constructor() {
        this.observers = new Array<Observer>();
    }
    
    public static create(): TimeEntriesIterator {
        return new TimeEntriesIterator();
    }
    
    public async calculateBikeRentalsAndReturns(path: string): Promise<void> {
        let it: HistoryIterator; 
        try {
            it = await HistoryIterator.create(path);
        }
        catch(error) {
            console.log('error creating history iterator:', error);
        }
        try {
            let timeEntry: TimeEntry = await it.nextTimeEntry();
       
            while(timeEntry !== undefined) {
                this.notify(timeEntry);
                timeEntry = await it.nextTimeEntry();
            }
        }
        catch(error) {
            console.log('error getting time entries:', error);
        }
        return;
    }
        
    public notify(timeEntry: TimeEntry): void {
        for(let observer of this.observers) {
            observer.update(timeEntry);
        }
    }
    
    public subscribe(observer: Observer): void {
        this.observers.push(observer);
    }

}
