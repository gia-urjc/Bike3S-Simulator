import { HistoryReader } from '../util';
import { HistoryTimeEntries } from '../../shared/history';

export class HistoryIterator {
	private currentFile: HistoryTimeEntries;
	private pointer: number;
	
	constructor() {
		this.file = await nextChangeFile();
		this.pointer = -1;
	}

	public async nextTimeEntry() {
			this.pointer++;
			let timeEntry = this.file[pointer];
			
			if (timeEntry == undefined) {
				this.file = await nextChangeFile();
				
				if (this.file != undefinded) {
					pointer = 0;
					timeEntry = this.file[pointer];
				}
				else {
					return undefined;
				}
			}
			return timeEntry;
		} 
	}
	
	public async previousTimeEntry() {
    if (pinter > 0) {
        pointer--;
        let timeEntry = this.file[pointer];
    }
        
}
	
	}
	 
    
} 