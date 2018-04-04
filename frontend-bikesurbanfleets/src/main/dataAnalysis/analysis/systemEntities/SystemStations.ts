import { HistoryEntitiesJson } from "../../../../shared/history";
import { HistoryReader } from "../../../util";
import { Station } from "../../systemDataTypes/Entities";

export class SystemStations {
    private stations: Array<Station>;
	
    public async init(history: HistoryReader): Promise<void> {
        try {
            let entities: HistoryEntitiesJson = await history.getEntities("stations");
            this.stations = <Station[]> entities.instances;
        }
        catch(error) {
            throw new Error('Error getting stations: '+error);
        }
        return;
    }
	
	public getStations(): Array<Station> {
	    return this.stations;
    }
    
}
