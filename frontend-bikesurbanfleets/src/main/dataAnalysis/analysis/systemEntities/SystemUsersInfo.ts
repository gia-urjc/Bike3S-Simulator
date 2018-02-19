import { HistoryReader } from '../../../../util';
import { HistoryEntitiesJson } from '../../../../../shared/history';
import  { User } from '../../../systemDataTypes/Entities';

export class SystemUsersInfo {
    private users: Array<User>;
    
    public async init(path: string): Promise<void> {
        try {
            let history: HistoryReader = await HistoryReader.create(path);
            let entities: HistoryEntitiesJson = await history.getEntities("users");
            this.users = entities.instances;
        }
        catch(error) {
            throw new Error('Error accessing to users: '+error);
        }
        return;
    }

    public getUsers(): Array<User> {
        return this.users;
    }
     
}