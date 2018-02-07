import { HistoryReader } from '../../../../util';
import { HistoryEntitiesJson } from '../../../../../shared/history';
import { HistoryIterator } from "../../../HistoryIterator";
import { Observer } from '../../ObserverPattern';
import  { Station } from '../../../systemDataTypes/Entities';
import  { TimeEntry, Event } from '../../../systemDataTypes/SystemInternalData';
import { BikesOfStation } from './BikesOfStation';

interface TimeInterval {
    start: number;
    end: number;
}

export class EmptyStation {
    private emptyIntervalsPerStation: Map<number, Array<TimeInterval>>;
    private emptyTimesPerStation: Map<number, number>;
    
    public constructor() {
        this.emptyIntervalsPerStation = new Map();
        this.emptyTimesPerStation = new Map();
    }

}
