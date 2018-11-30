export interface TimeEntry {
    time: number;
    events: Array<Event>;
}

export interface Event {
    name: string;
    order: number;
    changes: any;
    newEntities: any;
    oldEntities:any;   
    result: string;
    involvedEntities: any;
}

