import {HistoryTimeEntry} from "../../shared/history";
import {HistoryReader} from '../util';
import {TimeEntry, Event} from './systemDataTypes/SystemInternalData';

export class HistoryIterator {
    private history: HistoryReader;
    private currentFile: Array<HistoryTimeEntry>;
    private pointer: number;

    private constructor() {
        this.history = undefined;
        this.currentFile = undefined;
        this.pointer = -1;
    }

    public static async create(path: string): Promise<HistoryIterator> {
        let historyIt: HistoryIterator = new HistoryIterator();
        try {
            historyIt.history = await HistoryReader.create(path);
            historyIt.currentFile = await historyIt.history.nextChangeFile();
            return historyIt;
        }
        catch (error) {
            console.log('There is no available files:', error);
        }
    }

    public async nextTimeEntry(): Promise<TimeEntry> {
        let timeEntry: TimeEntry = undefined;
        if (this.currentFile !== undefined) {
            this.pointer++;
            timeEntry = this.currentFile[this.pointer];

            if (timeEntry === undefined) {
                try {
                    this.currentFile = await this.history.nextChangeFile();
                    this.pointer = 0;
                    timeEntry = this.currentFile[this.pointer];
                }
                catch (error) {
                    this.currentFile = undefined;
                }
            }
        }
        return timeEntry;
    }

    /**    
     public async previousTimeEntry(): Promise<any> {
         let timeEntry: any = undefined;
        if(this.currentFile !== undefined) {
            if(this.pointer > 0) {
                this.pointer--;
                timeEntry = this.currentFile[this.pointer];
            }
            else {
                try {
                    this.currentFile = await this.history.previousChangeFile();
                    this.pointer = this.currentFile.length-1;
                    timeEntry = this.currentFile[this.pointer]; 
            }
                catch(error) {
                    tcurrentFile = undefined;
                }
        }    
     }
    */
    
    public static getEventByName(timeEntry: TimeEntry, name: string): Event {
        let events: Array<Event> = timeEntry.events;
        let foundEvent: Event = undefined;
        for (let event of events) {
            if (event.name === name) {
                foundEvent = event;
                break;
            }
        }
        return foundEvent;
    }

}