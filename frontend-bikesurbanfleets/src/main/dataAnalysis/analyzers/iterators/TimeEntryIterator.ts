import { HistoryIterator } from '../../HistoryIterator';
import { TimeEntry } from '../../systemDataTypes/SystemInternalData';
import { Observer } from '../ObserverPattern';
import { Iterator } from './Iterator';
import { HistoryReader } from '../../HistoryReader';

export class TimeEntryIterator implements Iterator {
    private observers: Array<Observer>;
    private history: HistoryReader;
    
    public constructor() {
        this.observers = new Array<Observer>();
    }
    
    public setHistory(history: HistoryReader): void {
        this.history = history;
    }
    
    public iterate(): void {
        let it: HistoryIterator; 
        try {
            it = HistoryIterator.create(this.history);
            let timeEntry: TimeEntry | undefined = it.nextTimeEntry();
       
            while(timeEntry !== undefined) {
                this.notify(timeEntry);
                timeEntry = it.nextTimeEntry();
            }
        }
        catch(error) {
            throw new Error('Error getting time entries: '+ error);
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
