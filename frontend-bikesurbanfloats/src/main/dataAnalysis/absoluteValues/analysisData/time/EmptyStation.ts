import { HistoryReader } from '../../../../util';
import { HistoryEntitiesJson } from '../../../../../shared/history';
import { HistoryIterator } from "../../../HistoryIterator";
import { Observer } from '../../ObserverPattern';
import  { Station } from '../../../systemDataTypes/Entities';
import  { TimeEntry, Event } from '../../../systemDataTypes/SystemInternalData';

interface Interval {
    start: number;
    end: number;
}

export class EmptyStation implements Observer {
    private stations: Array<Station>;
    private emptyIntervalsPerStation: Map<number, Array<Interval>>;
    private emptyTimesPerStation: Map<number, number>;
    
    public constructor() {
        this.emptyIntervalsPerStation = new Map();
        this.emptyTimesPerStation = new Map();
    }
    
    public async init(path: string): Promise<boolean> {
        try {
            let history: HistoryReader = await HistoryReader.create(path);
            let entities: HistoryEntitiesJson = await history.getEntities('stations');
            this.stations = <Station[]> entities.instances;
        
            for(let station of this.stations) {
                this.emptyIntervalsPerStation.set(station.id, new Array());
                this.emptyTimesPerStation.set(station.id, number);
            }
            return true;
        }
        catch(error) {
            console.log('error getting stations:', error);
        }
    }
    
    public update(timeEntry: TimeEntry): void {
        
    }
    

}
