import { HistoryReader } from '../../../util';
import { HistoryIterator } from '../../HistoryIterator';
import { TimeEntry } from '../../systemDataTypes/SystemInternalData';
import { Observer, Observable } from '../ObserverPattern';
import { Calculator } from './Calculator';

export class RentalAndReturnCalculator implements Calculator {
    private observers: Array<Observer>;
    private path: string;
    
    public constructor(path: string) {
        this.observers = new Array<Observer>();
        this.path = path;
    }
    
<<<<<<< HEAD:frontend-bikesurbanfleets/src/main/dataAnalysis/analysis/calculators/RentalAndReturnCalculator.ts
    public async calculate(): Promise<void> {
        let it: HistoryIterator; 
        try {
            it = await HistoryIterator.create(this.path);
            let timeEntry: TimeEntry | undefined = await it.nextTimeEntry();
=======
    public async calculateBikeRentalsAndReturns(path: string, schemaPath?: string | null): Promise<void> {
        let it: HistoryIterator; 
        try {
            it = await HistoryIterator.create(path, schemaPath);
            let timeEntry: TimeEntry = await it.nextTimeEntry();
>>>>>>> d86b148f5d966d645a819dde4afc777d22832467:frontend-bikesurbanfleets/src/main/dataAnalysis/analysis/systemDataCalculators/RentalAndReturnCalculator.ts
       
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
