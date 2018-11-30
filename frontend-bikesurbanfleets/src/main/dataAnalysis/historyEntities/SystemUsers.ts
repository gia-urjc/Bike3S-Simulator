import { User } from "../systemDataTypes/Entities";

export class SystemUsers implements Observer{
    private users: Array<User>;
    
    public init(): void {
        this.users = [];
        return;
    }

    public getUsers(): Array<User> {
        return this.users;
    }
    
    public update(timeEntry: TimeEntry): void {
        for(let event of timeEntry.events) {
            if(event.name==='EventUserAppears') {
                const user: User=event.newEntities.users[0];
                this.users.push(user);
            }
        }
    }
 }    
      
