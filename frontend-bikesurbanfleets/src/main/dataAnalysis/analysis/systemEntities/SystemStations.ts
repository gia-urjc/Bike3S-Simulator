import { HistoryReader } from '../../../../util';
import { HistoryEntitiesJson } from '../../../../../shared/history';
import  { Station } from '../../../systemDataTypes/Entities';

export class SystemStations {
    private stations: Array<Station>;
	
    public async init(path: string): Promise<void> {
        try {
            let history: HistoryReader = await HistoryReader.create(path);
            let entities: HistoryEntitiesJson = await history.getEntities("stations");
            this.stations = <Station[]> entities.instances;
        }
        catch(error) {
            throw new Error('Error accessing to stations: '+error);
        }
        return;
    }
	
	public getStations(): Array<Station> {
	    return this.stations;
    }
    
}
