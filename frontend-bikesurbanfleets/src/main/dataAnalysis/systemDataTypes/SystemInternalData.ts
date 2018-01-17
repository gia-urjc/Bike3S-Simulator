export interface TimeEntry {
    time: number;
    events: Array<Event>;
}

export interface Event {
    name: string;
    changes: any;   
}

