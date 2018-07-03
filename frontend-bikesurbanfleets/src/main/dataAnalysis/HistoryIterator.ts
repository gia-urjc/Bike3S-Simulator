import {HistoryTimeEntry} from "../../shared/history";
import {HistoryReaderController} from '../util';
import {TimeEntry, Event} from './systemDataTypes/SystemInternalData';

export class HistoryIterator {
    private history: HistoryReaderController;
    private currentFile: Array<HistoryTimeEntry> | undefined;
    private pointer: number;   // pointer to the time entry is being currently  read

    public static async create(history: HistoryReaderController): Promise<HistoryIterator> {
        let historyIt: HistoryIterator = new HistoryIterator(history);
        try {
            historyIt.currentFile = await historyIt.history.nextChangeFile();
        }
        catch(error) {
            throw new Error('Error creating history iterator:'+error);
        }
        return historyIt;
    }

    private constructor(history: HistoryReaderController) {
        this.history = history;
        this.pointer = -1;
    }

    public async nextTimeEntry(): Promise<TimeEntry | undefined> {
        let timeEntry: TimeEntry | undefined = undefined;
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
    
    
}