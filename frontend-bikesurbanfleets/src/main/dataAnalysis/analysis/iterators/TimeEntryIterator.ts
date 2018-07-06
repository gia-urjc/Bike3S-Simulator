import { HistoryReaderController } from '../../../util';
import { HistoryIterator } from '../../HistoryIterator';
import { TimeEntry } from '../../systemDataTypes/SystemInternalData';
import { Observer, Observable } from '../ObserverPattern';
import { Iterator } from './Iterator';

export class TimeEntryIterator implements Iterator {
    private observers: Array<Observer>;
    private history: HistoryReaderController;
    
    public constructor() {
        this.observers = new Array<Observer>();
    }
    
    public setHistory(history: HistoryReaderController): void {
        this.history = history;
    }
    
    public async iterate(): Promise<void> {
        let it: HistoryIterator; 
        try {
            it = await HistoryIterator.create(this.history);
            let timeEntry: TimeEntry | undefined = await it.nextTimeEntry();
       
            while(timeEntry !== undefined) {
                this.notify(timeEntry);
                timeEntry = await it.nextTimeEntry();
            }
        }
        catch(error) {
            throw new Error('Error getting time entries: '+error);
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
