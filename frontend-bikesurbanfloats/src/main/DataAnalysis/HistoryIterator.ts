import { HistoryReader } from '../util';
import { HistoryTimeEntries } from '../../shared/history';

export class HistoryIterator {
	private currentFile: HistoryTimeEntries;
	private pointer: number;
    
    public async init(): void {
        history : HistoryReader;
        history = await HistoryReader.create("../backend-bikesurbanfloats/history");
        history.nextChangeFile().then( file -> this.file = file) 
        .catch( error -> {
            console.log(error);
            this.file = 
            undefined;
        });
    } 
	
 
    
} 





