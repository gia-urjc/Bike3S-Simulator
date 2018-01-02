import { HistoryReader } from '../util';
import { HistoryTimeEntries } from '../../shared/history';

export class HistoryIterator {
    private history: HistoryReader;
    private currentFile: HistoryTimeEntries;
    private pointer: number;
           
    public constructor() {
        this.history = undefined;
        this.currentFile = undefined;
        this.pointer = -1;
    }
    
    public static async init(path: string) {
        this.history = await HistoryReader.create(path);
        
        this.history.nextChangeFile().then((data) => this.currentFile = data) 
        .catch((error) => console.log('There is no available files ', error));
    } 
    
 public async nextTimeEntry(): Promise<any> {
     let timeEntry: any = undefined;
     if (this.currentFile !== undefined) {
        this.pointer++;
        timeEntry = this.currentFile[this.pointer];
       
       if (timeEntry === undefined) {
           this.history.nextChangeFile().then((data) => { 
               this.currentFile = data;
               this.pointer = 0; 
               timeEntry = this.currentFile[this.pointer]; 
           })
           .catch((error) => { 
               console.log('There iss no more available files: ', error);
               this.currentFile = undefined;
           });
       }
     }
     return timeEntry;
 }


    
}