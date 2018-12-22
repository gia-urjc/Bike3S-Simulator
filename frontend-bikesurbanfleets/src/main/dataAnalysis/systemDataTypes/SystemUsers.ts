import HistoryUserChanges from "../historyEntities/HistoryUserChanges";
import { TimeEntry } from "./SystemInternalData";
import { Observer } from "../analyzers/ObserverPattern";

export class SystemUsers implements Observer{
    private users: Array<HistoryUserChanges>;
    
    public init(): void {
        this.users = [];
        return;
    }

    public getUsers(): Array<HistoryUserChanges> {
        return this.users;
    }
    
    public update(timeEntry: TimeEntry): void {
        for(let event of timeEntry.events) {
            if(event.name==='EventUserAppears') {
                const user: HistoryUserChanges = event.newEntities.users[0];
                this.users.push(user);
            }
        }
    }
 }    
      
