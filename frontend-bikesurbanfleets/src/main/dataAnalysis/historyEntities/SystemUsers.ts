import { HistoryEntitiesJson } from "../../../shared/history";
import { User } from "../systemDataTypes/Entities";
import { HistoryReader } from "../HistoryReader";

export class SystemUsers {
    private users: Array<User>;
    
    public init(history: HistoryReader): void {
        try {
            let entities: HistoryEntitiesJson = history.getEntities("users");
            this.users = entities.instances;
        }
        catch(error) {
            throw new Error('Error getting users: '+error);
        }
        return;
    }

    public getUsers(): Array<User> {
        return this.users;
    }
     
}