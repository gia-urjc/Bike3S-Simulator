import { HistoryEntitiesJson } from "../../../../shared/history";
import { HistoryReaderController } from "../../../util";
import { User } from "../../systemDataTypes/Entities";

export class SystemUsers {
    private users: Array<User>;
    
    public async init(history: HistoryReaderController): Promise<void> {
        try {
            let entities: HistoryEntitiesJson = await history.getEntities("users");
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